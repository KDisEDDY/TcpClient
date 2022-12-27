package com.ljy.tcpclientlib

import android.content.Context
import android.util.Log
import com.ljy.tcpclientlib.seeker.ConnectThread
import java.util.concurrent.LinkedBlockingQueue
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

    private var isStartThread = AtomicBoolean(false)

    private var connectBlockQueue = LinkedBlockingQueue<Connection>()
    private var connectThread: ConnectThread? = null

    override fun connection(connection: Connection) {
        if (isStartThread.compareAndSet(false, true)) {
            loop()
        }
        connectBlockQueue.put(connection)
    }

    /**
     * 单线程轮询connect操作，通过BlockQueue调用连接
     */
    private fun loop() {
        connectThread = ConnectThread(context, this, connectBlockQueue).apply {
            initConfig()
            start()
        }
    }

    /**
     * 判断是否有写入通道的数据，todo 之后通过队列数据结构来设置
     */
    private fun hasReadyToWrite() = false

    override fun disconnect(channelId: Int) {
        try {
            // 关闭连接线程
            selectorThreadGroup?.disconnect(channelId)
        } catch (e: Throwable) {
            Log.e(TAG, "socket close failed cause: ${e.message}\n  stack ${e.stackTrace}")
        }
    }

    override fun disconnectAll() {
        try {
            // 关闭连接线程
            connectThread?.interrupt()
            selectorThreadGroup?.disconnect()
            isStartThread.set(false)
        } catch (e: Throwable) {
            Log.e(TAG, "socket close failed cause: ${e.message}\n  stack ${e.stackTrace}")
        }
    }
}