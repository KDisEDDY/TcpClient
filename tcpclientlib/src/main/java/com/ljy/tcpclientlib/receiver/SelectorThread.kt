package com.ljy.tcpclientlib.receiver

import android.util.Log
import com.ljy.tcpclientlib.Constant
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.nio.channels.SocketChannel
import java.util.concurrent.LinkedBlockingQueue

/**
 * @author Eddy.Liu
 * @Date 2022/12/14
 * @Description
 **/
class SelectorThread(val selectorThreadGroup: SelectorThreadGroup) : Runnable {

    companion object {
        private const val TAG = "${Constant.CLIENT_LOG}_SelectorThread"
    }
    lateinit var selector: Selector
        private set
    lateinit var queue: LinkedBlockingQueue<SocketChannel>
        private set


    init {
        try {
            selector = Selector.open()
            queue = LinkedBlockingQueue()

        } catch (t: Throwable) {
            Log.e(TAG, "init failed ${t.stackTrace}")
        }
    }

    override fun run() {
        try {
            while (true) {
                val num = selector.select()
                if (num > 0) {
                    val iterator = selector.selectedKeys()?.iterator()
                    while (iterator?.hasNext() == true) {
                        val key = iterator.next()
                        iterator.remove()

                        if (key.isReadable) {

                        }
                    }
                }
                while (!queue.isEmpty()) {
                    queue.take()?.let {
                        it.configureBlocking(false)
                        it.register(selector, SelectionKey.OP_READ)
                    }

                }
            }
        } catch (t: Throwable) {
            Log.e(TAG, "run thread failed ${t.stackTrace}")
        }


    }
}