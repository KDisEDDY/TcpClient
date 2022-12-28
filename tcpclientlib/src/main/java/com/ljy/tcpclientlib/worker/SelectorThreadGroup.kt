package com.ljy.tcpclientlib.worker

import android.util.Log
import com.ljy.tcpclientlib.Constant
import java.nio.channels.SocketChannel
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * @author Eddy.Liu
 * @Date 2022/12/14
 * @Description
 **/
class SelectorThreadGroup {

    companion object {
        private const val TAG = "${Constant.CLIENT_LOG}_SelectorThreadGroup"
    }

    private val selectorThreads = ConcurrentHashMap<Int, WorkerRunnable>()
    private val executorService: ExecutorService = Executors.newCachedThreadPool()

    fun register(id: Int, socketChannel: SocketChannel, responseDispatcher: ConcurrentHashMap<Int, ResponseHandler>) {
        try {
            selectorThreads[id] = WorkerRunnable()
            executorService.execute(selectorThreads[id])
            selectorThreads[id]?.let {
                it.channelId = id
                it.responseDispatcher = responseDispatcher
                it.queue.put(socketChannel)
                it.selector.wakeup()
            }
        } catch (e: InterruptedException) {
            Log.e(TAG, "${e.stackTrace}")
        }
    }

    fun disconnect(isNeedRemoveHandler: Boolean = false) {
        selectorThreads.forEach {
            it.value.disconnect(isNeedRemoveHandler)
        }
    }

    fun disconnect(id: Int) {
        selectorThreads.forEach {
            if (it.key == id) {
                it.value.disconnect(true)
            }
        }
    }

}