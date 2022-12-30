package com.ljy.tcpclientlib.worker

import android.util.Log
import com.ljy.tcpclientlib.Constant
import com.ljy.tcpclientlib.TcpClient
import com.ljy.tcpclientlib.io.NIO
import com.ljy.tcpclientlib.seeker.ConnectThread
import java.io.IOException
import java.nio.channels.ClosedChannelException
import java.nio.channels.ClosedSelectorException
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.nio.channels.SocketChannel
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.atomic.AtomicBoolean

/**
 * @author Eddy.Liu
 * @Date 2022/12/14
 * @Description 读写操作的实际task类
 **/
class WorkerRunnable : Runnable {

    companion object {
        private const val TAG = "${Constant.CLIENT_LOG}_WorkerRunnable"
    }
    lateinit var selector: Selector
        private set
    lateinit var queue: LinkedBlockingQueue<SocketChannel>
        private set
    var responseDispatcher: ConcurrentHashMap<Int, ResponseHandler>? = null
    var channelId: Int = 0

    private val isConnection = AtomicBoolean(false)
    private var nio: NIO? = null
    private var socketChannel: SocketChannel? = null
    init {
        try {
            selector = Selector.open()
            queue = LinkedBlockingQueue()

        } catch (t: Throwable) {
            Log.e(TAG, "init failed ${t.message}")
        }
    }

    override fun run() {
        try {
            if (isConnection.compareAndSet(false, true)) {
                while (true) {
                    while (!queue.isEmpty()) {
                        queue.take()?.let {
                            this.socketChannel = it
                            it.configureBlocking(false)
                            it.register(selector, SelectionKey.OP_READ)
                        }

                    }
                    val num = selector.select()  // 阻塞，外部通过wakeup当前selector来唤醒
                    if (!isConnection.get()) {
                        // 这里再判断一遍的原因是外部已经触发了关闭通道的操作，由于线程可能还在执行中，直接在这里关闭
                        Log.i(TAG, "the thread of channel is closing ")
                        return
                    }
                    if (num > 0) {
                        val iterator = selector.selectedKeys()?.iterator()
                        while (iterator?.hasNext() == true) {
                            val key = iterator.next()
                            iterator.remove()

                            if (key.isReadable) {
                                readOps(key)
                            }
                        }
                    }

                }
            }
        }  catch (e: ClosedChannelException) {
            Log.e(TAG, "this channel is closed :" + e.message)
        } catch (e: ClosedSelectorException) {
            Log.e(TAG, "this selector is closed :" + e.message)
        } catch (t: Throwable) {
            Log.e(TAG, "run thread failed ${t.message}")
            close(true)
        }
    }

    fun disconnect(isNeedRemoveHandler: Boolean = false) {
        if (selector.isOpen && isConnection.compareAndSet(true, false)) {
            close(isNeedRemoveHandler)
        }
    }

    private fun close(isNeedRemoveHandler: Boolean = false) {
        try {
            selector.close()
            socketChannel?.close()
            socketChannel = null
        } catch (e: IOException) {
            Log.e(TAG, "disconnecting when a IOException occur " + e.message)
        }
        if (isNeedRemoveHandler) {
            responseDispatcher?.remove(channelId)
        }
        Log.i(TAG, "the runnable is disconnected ")
    }

    private fun readOps(key: SelectionKey) {
        if (nio == null) {
            (key.channel() as? SocketChannel)?.let {
                nio = NIO(it)
            }
        }
        nio?.read()?.let {
            responseDispatcher?.get(channelId)?.onReadResponse(it)
        }
    }
}