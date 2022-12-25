package com.ljy.tcpclientlib

import android.content.Context
import android.util.Log
import com.ljy.tcpclientlib.exceptions.ConnectionFailedException
import com.ljy.tcpclientlib.receiver.ResponseHandler
import com.ljy.tcpclientlib.seeker.ConnectThread
import java.io.IOException
import java.nio.channels.AlreadyConnectedException
import java.nio.channels.ClosedChannelException
import java.nio.channels.ConnectionPendingException
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.nio.channels.SocketChannel
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.atomic.AtomicBoolean

/**
 * @author Eddy.Liu
 * @Date 2022/12/8
 * @Description
 **/
class TcpClient(context: Context, num: Int) : AbsTcpClient(context, num){

    companion object {
        const val TAG = "${Constant.CLIENT_LOG}_TcpClient"
    }

    private var connectSelector = Selector.open()
    private var isStartThread = AtomicBoolean(false)

    private var connectBlockQueue = LinkedBlockingQueue<Connection>()
    private var connectThread = ConnectThread(context, num, this)

    override fun connection(connection: Connection) {
        connectBlockQueue.put(connection)
        if (isStartThread.compareAndSet(false, true)) {
            loop()
        }
    }

    /**
     * 单线程轮询connect操作，通过BlockQueue调用连接
     */
    private fun loop() {
        connectThread.start()
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
                connectThread.interrupt()
            }
            selectorThreadGroup?.disconnect()
        } catch (e: Throwable) {
            Log.e(TAG, "socket close failed cause: ${e.message}\n  stack ${e.stackTrace}")
        }
    }
}