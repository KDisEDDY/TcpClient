package com.ljy.tcpclientlib.receiver

import android.util.Log
import com.ljy.tcpclientlib.Constant
import java.nio.channels.SocketChannel
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

/**
 * @author Eddy.Liu
 * @Date 2022/12/14
 * @Description
 **/
class SelectorThreadGroup(num: Int) {

    companion object {
        private const val TAG = "${Constant.CLIENT_LOG}_SelectorThreadGroup"
    }

    private val selectorThreads = arrayOfNulls<SelectorThread>(num)
    private val indexCtl = AtomicInteger(0)

    init {
        for(i in 0 until num) {
            selectorThreads[i] = SelectorThread()
            Thread(selectorThreads[i]).start()
        }
    }

    fun register(id: Int, socketChannel: SocketChannel, responseDispatcher: ConcurrentHashMap<Int, ResponseHandler>) {
        try {
            val index = indexCtl.incrementAndGet() % selectorThreads.size
            selectorThreads[index]?.let {
                it.channelId = id
                it.responseDispatcher = responseDispatcher
                it.queue.put(socketChannel)
                it.selector.wakeup()
            }
        } catch (e: InterruptedException) {
            Log.e(TAG, "${e.stackTrace}")
        }
    }

    fun disconnect() {
        selectorThreads.forEach {
            it?.disconnect()
        }
    }

}