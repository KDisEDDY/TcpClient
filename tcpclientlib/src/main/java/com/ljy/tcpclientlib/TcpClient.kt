package com.ljy.tcpclientlib

import android.content.Context
import android.util.Log
import com.ljy.tcpclientlib.exceptions.ConnectionFailedException
import com.ljy.tcpclientlib.interfaces.IResponseStateHandler
import com.ljy.tcpclientlib.receiver.ResponseHandler
import com.ljy.tcpclientlib.receiver.SelectorThreadGroup
import java.io.IOException
import java.nio.channels.AlreadyConnectedException
import java.nio.channels.ClosedChannelException
import java.nio.channels.ConnectionPendingException
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.nio.channels.SocketChannel
import java.util.concurrent.ConcurrentHashMap

/**
 * @author Eddy.Liu
 * @Date 2022/12/8
 * @Description
 **/
class TcpClient(context: Context) : AbsTcpClient(context){

    companion object {
        const val TAG = "${Constant.CLIENT_LOG}_TcpClient"
    }

    var connectSelector: Selector? = null

    private var selectorThreadGroup: SelectorThreadGroup? = null

    private var responseDispatcher = ConcurrentHashMap<Int, ResponseHandler>()

    override fun connection(ip: String?, port: Int) {
        // 这里还是有点问题，估计不能通过new线程来启动通道的连接操作，而是应该类似SelectThread一样，通过BlockQueue的方式调用连接，todo 之后再来改
        Thread {
            var isHasException = false
            var id = 0
            // 调用 SelectorProvider 通过SPI机制 + 反射生成对应的Selector实例
            try {
                id = openChannel(ip, port)
                connectSelector = Selector.open()
            } catch (e: IOException) {
                Log.e(TAG, "${e.stackTrace}")
                isHasException = true
            }

            try {
                getSocketChannel(id)?.register(connectSelector, SelectionKey.OP_CONNECT)
                connect(id)
            } catch (e: AlreadyConnectedException) {
                Log.e(TAG, "this channel is already connected :" + e.message)
                isHasException = true
            } catch (e: ConnectionPendingException) {
                Log.e(TAG, "a non-blocking connecting operation is already executing on this channel :" + e.message)
                isHasException = true
            } catch (e: ClosedChannelException) {
                Log.e(TAG, "this channel is closed :" + e.message)
                isHasException = true
            } catch (e: Exception) {
                Log.e(TAG, "connection failed :" + e.message)
                isHasException = true
            }

            if (!isHasException) {
                listenConnection(id)
            }
        }.start()
    }

    /**
     * 热流，注册回调和有无数据流出无关
     */
    fun registerResponseHandler(id: Int, responseHandler: ResponseHandler) {
        if (responseDispatcher[id] == null) {
            responseDispatcher[id] = responseHandler
        }
    }

    private fun listenConnection(id: Int) {
        try {
            // 当前线程轮询，查询selector有哪些key可以
            while (true) {
                connectSelector?.select()
                val iterator = connectSelector?.selectedKeys()?.iterator()
                while (iterator?.hasNext() != false) {
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
                            getSocketChannel(id)?.let {
                                selectorThreadGroup?.register(id, it, responseDispatcher)
                            }
                        }
                    }
                }
            }
        } catch(e: Throwable) {
            Log.e(TAG, "listen connect fail ${e.message}")
            try {
                // 关闭读写
                if (connectSelector != null && connectSelector?.isOpen == true) {
                    connectSelector?.close()
                    connectSelector = null
                }
            } catch (e: Throwable) {
                Log.e(TAG, "socket close failed cause: ${e.message}\n  stack ${e.stackTrace}")
            }
        }

    }

    /**
     * 判断是否有写入通道的数据，todo 之后通过队列数据结构来设置
     */
    private fun hasReadyToWrite() = false

    override fun disconnect() {
        try {
            // 关闭读写
            if (connectSelector != null && connectSelector?.isOpen == true) {
                connectSelector?.close()
                connectSelector = null
            }
            selectorThreadGroup?.disconnect()
        } catch (e: Throwable) {
            Log.e(TAG, "socket close failed cause: ${e.message}\n  stack ${e.stackTrace}")
        }
    }

    private fun isConnected(id: Int, careNet: Boolean): Boolean {
        return getSocketChannel(id)?.isConnected == true && if (careNet) NetUtils.netIsAvailable(context) else true
    }


}