package com.ljy.tcpclientlib.seeker

import android.content.Context
import android.util.Log
import com.ljy.tcpclientlib.*
import com.ljy.tcpclientlib.exceptions.ConnectionFailedException
import com.ljy.tcpclientlib.io.NIO
import com.ljy.tcpclientlib.worker.WorkerRunnable
import java.io.IOException
import java.nio.channels.*
import java.util.concurrent.LinkedBlockingQueue

class ConnectThread(private val context: Context, private val tcpClient: AbsTcpClient, private val connectBlockQueue: LinkedBlockingQueue<Connection>):
    Thread() {
    companion object {
        private const val TAG = "${Constant.CLIENT_LOG}_ConnectThread"
        //
        private const val SELECT_CONNECTING_THRESHOLD = 512
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
                tcpClient.registerWorkerThread(connection.channelId)
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
            } else {
                // 有问题了就关闭当前的channel connection
                tcpClient.disconnect(connection.channelId)
            }
        }
    }

    private fun listenConnection(id: Int) {
        try {
            // 这里会有Epoll空轮询问题，为了避免有connection操作被遗漏，通过hasConnected变量来判断是否有处理了connection，没有的话继续轮询；
            // 当hasConnected为true，退出轮询，回到connectBlockQueue 等待下一个connection的接入
            var hasConnected = false
            var cnt = 0
            // 当前线程轮询，查询selector有哪些key可以
            while (cnt < SELECT_CONNECTING_THRESHOLD) {
                connectSelector?.select(Constant.SELECT_TIMEOUT)
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
//                            tcpClient.registerWorkerThread(id)
                            Log.i(TAG, "connect success")
                            hasConnected = true
                        }
                    }
                }
                if (hasConnected) {
                    Log.i(TAG, "has built the connection channel id $id, return the function")
                    return
                }
                cnt++
            }
            if (cnt >= SELECT_CONNECTING_THRESHOLD) {
                // 当超过了connection 操作的轮询次数SELECT_CONNECTING_THRESHOLD， 证明当前链接是有问题的，直接关闭
                Log.e(TAG, "the connect has reach the max select invocation count, it's WRONG, disconnect")
                tcpClient.disconnect(id)
            }
        } catch(e: Throwable) {
            Log.e(TAG, "listen connect fail ${e.message}")
            try {
                // 关闭读写
                if (connectSelector != null && connectSelector?.isOpen == true) {
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
        val isConnected = tcpClient.getSocketChannel(id)?.isConnected == true
        val isNetAvailable = NetUtils.netIsAvailable(context)
        Log.i(TAG, "isConnected $isConnected isNetAvailable $isNetAvailable ")
        return isConnected && isNetAvailable
    }
}