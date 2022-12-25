package com.ljy.tcpclientlib.seeker

import android.content.Context
import android.util.Log
import com.ljy.tcpclientlib.AbsTcpClient
import com.ljy.tcpclientlib.Connection
import com.ljy.tcpclientlib.NetUtils
import com.ljy.tcpclientlib.TcpClient
import com.ljy.tcpclientlib.exceptions.ConnectionFailedException
import com.ljy.tcpclientlib.receiver.SelectorThreadGroup
import java.io.IOException
import java.nio.channels.*
import java.util.concurrent.LinkedBlockingQueue

class ConnectThread(private val context: Context, channelNum: Int, private val tcpClient: AbsTcpClient):
    Thread() {
    private var connectSelector = Selector.open()
    private var connectBlockQueue = LinkedBlockingQueue<Connection>()
    private var selectorThreadGroup: SelectorThreadGroup? = null

    init {
        selectorThreadGroup = SelectorThreadGroup(channelNum)
    }

    override fun run() {
        while (true) {
            var isHasException = false
            // 调用 SelectorProvider 通过SPI机制 + 反射生成对应的Selector实例
            val connection = connectBlockQueue.take()
            try {
                tcpClient.openChannel(connection)
            } catch (e: IOException) {
                Log.e(TcpClient.TAG, "${e.stackTrace}")
                isHasException = true
            }

            try {
                tcpClient.getSocketChannel(connection.channelId)?.register(
                    connectSelector,
                    SelectionKey.OP_CONNECT
                )
                // 由于是异步模式，链接成功的判断不准确
                tcpClient.connect(connection.channelId)
            } catch (e: AlreadyConnectedException) {
                Log.e(TcpClient.TAG, "this channel is already connected :" + e.message)
                isHasException = true
            } catch (e: ConnectionPendingException) {
                Log.e(
                    TcpClient.TAG,
                    "a non-blocking connecting operation is already executing on this channel :" + e.message
                )
                isHasException = true
            } catch (e: ClosedChannelException) {
                Log.e(TcpClient.TAG, "this channel is closed :" + e.message)
                isHasException = true
            } catch (e: Exception) {
                Log.e(TcpClient.TAG, "connection failed :" + e.message)
                isHasException = true
            }

            if (!isHasException) {
                val hasSetResponse = connection.responseHandler?.let {
                    tcpClient.registerResponseHandler(connection.channelId, it)
                } ?: false
                // 判断是否有监听回调，如果设置了监听才证明首次设置了回调，才开启轮询监听
                if (hasSetResponse) {
                    listenConnection(connection.channelId)
                } else {
                    Log.e(TcpClient.TAG, "don't set response handler , had set it ")
                }
            }
        }
    }

    private fun listenConnection(id: Int) {
        try {
            // 当前线程轮询，查询selector有哪些key可以
            while (true) {
                connectSelector?.select()
                val iterator = connectSelector?.selectedKeys()?.iterator()
                while (iterator?.hasNext() != false) {
                    Log.i(TcpClient.TAG, "listen to connection channelId: $id")
                    val selectorKey = iterator?.next()
                    iterator?.remove()
                    // 当前有可连接事件 且连接已ready
                    if (selectorKey?.isConnectable == true) {
                        val channel = selectorKey.channel() as? SocketChannel
                        if (channel?.isConnectionPending == true) {
                            // 当前socket已经连接成功，可以关闭
                            while(!channel.finishConnect()) {
                                // 有可能当前通道还没连接完成，轮询去等待连接成功
                            }

                            if (!isConnected(id, true)) {
                                throw ConnectionFailedException("maybe network is not available")
                            }
                            //通过 SelectThreadGroup注册通道读操作，在单独线程里面启动读写
                            tcpClient.getSocketChannel(id)?.let {
                                selectorThreadGroup?.register(id, it, tcpClient.responseDispatcher)
                            }
                            Log.i(TcpClient.TAG, "connect success")
                        }
                    }
                }
                return
            }
        } catch(e: Throwable) {
            Log.e(TcpClient.TAG, "listen connect fail ${e.message}")
            try {
                // 关闭读写
                if (connectSelector != null && connectSelector?.isOpen == true) {
                    connectSelector?.close()
                    connectSelector = null
                    tcpClient.responseDispatcher.remove(id)
                }
            } catch (e: Throwable) {
                Log.e(TcpClient.TAG, "socket close failed cause: ${e.message}\n  stack ${e.stackTrace}")
            }
        }

    }

    private fun isConnected(id: Int, careNet: Boolean): Boolean {
        return tcpClient.getSocketChannel(id)?.isConnected == true && if (careNet) NetUtils.netIsAvailable(context) else true
    }
}