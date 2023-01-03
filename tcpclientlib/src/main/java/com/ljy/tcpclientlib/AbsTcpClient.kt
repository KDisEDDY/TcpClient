package com.ljy.tcpclientlib

import android.content.Context
import com.ljy.tcpclientlib.exceptions.OpenChannelException
import com.ljy.tcpclientlib.interfaces.IConnection
import com.ljy.tcpclientlib.interfaces.IDisconnect
import com.ljy.tcpclientlib.worker.ResponseHandler
import com.ljy.tcpclientlib.worker.SelectorThreadGroup
import java.io.IOException
import java.net.InetSocketAddress
import java.nio.channels.SocketChannel
import java.util.concurrent.ConcurrentHashMap

/**
 * @author Eddy.Liu
 * @Date 2022/12/7
 * @Description client的接口
 **/
abstract class AbsTcpClient(val context: Context) : IConnection, IDisconnect {

    private var isUnexpectedDisConnection = false

    protected var selectorThreadGroup: SelectorThreadGroup? = null

    private var inetSocketAddressMap = ConcurrentHashMap<Int, InetSocketAddress>()

    private val socketChannelMap = ConcurrentHashMap<Int, SocketChannel>()

    var responseDispatcher = ConcurrentHashMap<Int, ResponseHandler>()


    init {
        selectorThreadGroup = SelectorThreadGroup()
    }

    @Throws(OpenChannelException::class)
    open fun openChannel(connection: Connection) {
        isUnexpectedDisConnection = false
        val inetSocketAddress = InetSocketAddress(connection.ip, connection.port)
        inetSocketAddressMap[connection.channelId] = inetSocketAddress
        try {
            val socketChannel = SocketChannel.open()
            socketChannel?.configureBlocking(false)
            socketChannelMap[connection.channelId] = socketChannel
        } catch (e: IOException) {
            throw OpenChannelException(e)
        }
    }

    @Throws(IOException::class)
    open fun connect(id: Int) {
        val socketChannel = socketChannelMap[id]
        val inetSocketAddress = inetSocketAddressMap[id]
        if (socketChannel == null || inetSocketAddress == null) {
            throw IOException("channel need open first")
        }
        socketChannel.connect(inetSocketAddress)
    }

    fun getSocketChannel(id: Int) = socketChannelMap[id]

    /**
     * 热流，注册回调和有无数据流出无关
     */
    fun registerResponseHandler(id: Int, responseHandler: ResponseHandler): Boolean {
        return if (responseDispatcher[id] == null) {
            responseDispatcher[id] = responseHandler
            true
        } else false
    }

    fun hasSetResponseHandler(id: Int): Boolean {
        return responseDispatcher[id] != null
    }

    fun registerWorkerThread(id: Int) {
        //通过 SelectThreadGroup注册通道读操作，在单独线程里面启动读写
        getSocketChannel(id)?.let {
            selectorThreadGroup?.register(id, it, responseDispatcher)
        }
    }
}