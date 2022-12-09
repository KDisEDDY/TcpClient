package com.ljy.tcpclientlib

import android.content.Context
import android.util.Log
import com.ljy.tcpclientlib.IO.NIO
import com.ljy.tcpclientlib.exceptions.ConnectionFailedException
import java.io.IOException
import java.nio.channels.AlreadyConnectedException
import java.nio.channels.ClosedChannelException
import java.nio.channels.ConnectionPendingException
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.nio.channels.SocketChannel
import java.util.concurrent.atomic.AtomicBoolean

/**
 * @author Eddy.Liu
 * @Date 2022/12/8
 * @Description
 **/
class TcpClient(context: Context) : AbsTcpClient(context){

    companion object {
        const val TAG = "${Constant.CLIENT_LOG}_TcpClient"
    }

    var selector: Selector? = null

    private val isConnection = AtomicBoolean(false)

    private var nio: NIO? = null

    override fun connection(ip: String?, port: Int) {
        if (isConnection.compareAndSet(false, true)) {
            Thread {
                var isHasException = false
                // 调用 SelectorProvider 通过SPI机制 + 反射生成对应的Selector实例
                try {
                    openChannel(ip, port)
                    selector = Selector.open()
                } catch (e: IOException) {
                    Log.e(TAG, "${e.stackTrace}")
                    isHasException = true
                }

                try {
                    mSocketChannel?.register(selector, SelectionKey.OP_CONNECT)
                    connect()
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
                    listenConnection()
                }
            }.start()
        }
    }

    private fun listenConnection() {
        try {
            // 当前线程轮询，查询selector有哪些key可以
            while (true) {
                selector?.select()
                val iterator = selector?.selectedKeys()?.iterator()
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

                            if (!isConnected(true)) {
                                throw ConnectionFailedException("maybe network is not available")
                            }
                            // 注册通道读操作，在下一个轮询中启动读
                            mSocketChannel?.register(selector, SelectionKey.OP_READ)
                        }
                    } else if (selectorKey?.isReadable == true) {
                        readOps()
                    } else if (selectorKey?.isWritable == true && hasReadyToWrite()) {
                        writeOps()
                    }
                }
            }
        } catch(e: Throwable) {
            Log.e(TAG, "listen connect fail ${e.message}")
            try {
                // 关闭读写
                if (selector != null && selector?.isOpen == true) {
                    selector?.close()
                    selector = null
                }
                mSocketChannel?.close()
                mSocketChannel = null
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

    }

    /**
     * 读操作
     */
    private fun readOps() {
        if (nio == null) {
            nio = NIO(mSocketChannel)
        }
        nio?.read()?.let {
            // 给到外部的接收器
        }
    }

    private fun writeOps() {
        if (nio == null) {
            nio = NIO(mSocketChannel)
        }
    }

    private fun isConnected(careNet: Boolean): Boolean {
        return mSocketChannel?.isConnected == true && if (careNet) NetUtils.netIsAvailable(context) else true
    }


}