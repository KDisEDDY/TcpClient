package com.ljy.tcpclientlib.seeker

import android.content.Context
import android.util.Log
import com.ljy.tcpclientlib.*
import com.ljy.tcpclientlib.exceptions.ConnectionFailedException
import java.io.IOException
import java.nio.channels.*
import java.util.concurrent.LinkedBlockingQueue

class ConnectThread(private val context: Context, private val tcpClient: AbsTcpClient, private val connectBlockQueue: LinkedBlockingQueue<Connection>):
    Thread() {
    companion object {
        private const val TAG = "${Constant.CLIENT_LOG}_ConnectThread"
    }
    private var connectSelector: Selector? = null

    /**
     * init the thread for loop
     */
    fun initConfig() {
        connectBlockQueue.clear()
        connectSelector = Selector.open()
    }

    override fun run() {
        checkNotNull(connectSelector)
        while (true) {
            // 处理整个连接线程关闭的情况
            if (interrupted()) {
                interruptConnection()
                Log.d(TAG, "the connectThread is interrupted")
                return
            }
            var isHasException = false
            // 调用 SelectorProvider 通过SPI机制 + 反射生成对应的Selector实例

            val connection = try {
                connectBlockQueue.take()
            } catch (e: InterruptedException) {
                Log.d(TAG, "InterruptedException, ${e.message}")
                interruptConnection()
                return
            }
            if (tcpClient.hasSetResponseHandler(connection.channelId)) {
                Log.d(TAG, "hasSetResponseHandler, continue")
                continue
            }
            try {
                tcpClient.openChannel(connection)
            } catch (e: IOException) {
                Log.e(TAG, "${e.stackTrace}")
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
                Log.e(TAG, "this channel is already connected :" + e.message)
                isHasException = true
            } catch (e: ConnectionPendingException) {
                Log.e(
                    TAG,
                    "a non-blocking connecting operation is already executing on this channel :" + e.message
                )
                isHasException = true
            } catch (e: ClosedChannelException) {
                Log.e(TAG, "this channel is closed :" + e.message)
                isHasException = true
            } catch (e: Exception) {
                Log.e(TAG, "connection failed :" + e.message)
                isHasException = true
            }

            if (!isHasException) {
                connection.responseHandler?.let {
                    tcpClient.registerResponseHandler(connection.channelId, it)
                }
                // 如果设置了监听才证明首次设置了回调，才开启轮询监听
                listenConnection(connection.channelId)
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
                    Log.i(TAG, "listen to connection channelId: $id")
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
                            tcpClient.registerWorkerThread(id)
                            Log.i(TAG, "connect success")
                        }
                    }
                }
                return
            }
        } catch(e: Throwable) {
            Log.e(TAG, "listen connect fail ${e.message}")
            try {
                // 关闭读写
                if (connectSelector != null && connectSelector?.isOpen == true) {
                    connectSelector?.close()
                    connectSelector = null
                    tcpClient.responseDispatcher.remove(id)
                }
            } catch (e: Throwable) {
                Log.e(TAG, "socket close failed cause: ${e.message}\n  stack ${e.stackTrace}")
            }
        }
    }

    /**
     * 关闭整个连接
     */
    private fun interruptConnection() {
        try {
            // 关闭读写
            if (connectSelector != null && connectSelector?.isOpen == true) {
                connectSelector?.close()
                connectSelector = null
                tcpClient.responseDispatcher.clear()
            }
        } catch (e: Throwable) {
            Log.e(TAG, "socket close failed cause: ${e.message}\n  stack ${e.stackTrace}")
        }
    }

    private fun isConnected(id: Int, careNet: Boolean): Boolean {
        return tcpClient.getSocketChannel(id)?.isConnected == true && if (careNet) NetUtils.netIsAvailable(context) else true
    }
}