package com.ljy.tcpclientlib

import android.content.Context
import com.ljy.tcpclientlib.exceptions.OpenChannelException
import com.ljy.tcpclientlib.interfaces.IConnection
import com.ljy.tcpclientlib.interfaces.IDisconnect
import com.ljy.tcpclientlib.receiver.SelectorThreadGroup
import java.io.IOException
import java.net.InetSocketAddress
import java.nio.channels.SocketChannel

/**
 * @author Eddy.Liu
 * @Date 2022/12/7
 * @Description client的接口
 **/
abstract class AbsTcpClient(val context: Context, val channelNum: Int) : IConnection, IDisconnect {

    private var isUnexpectedDisConnection = false

    protected var selectorThreadGroup: SelectorThreadGroup? = null

    private var inetSocketAddressMap = mutableMapOf<Int, InetSocketAddress>()

    private val socketChannelMap = mutableMapOf<Int, SocketChannel>()

    init {
        selectorThreadGroup = SelectorThreadGroup(channelNum)
    }

    @Throws(OpenChannelException::class)
    protected open fun openChannel(connection: Connection) {
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
    protected open fun connect(id: Int) {
        val socketChannel = socketChannelMap[id]
        val inetSocketAddress = inetSocketAddressMap[id]
        if (socketChannel == null || inetSocketAddress == null) {
            throw IOException("channel need open first")
        }
        socketChannel.connect(inetSocketAddress)
    }

    fun getSocketChannel(id: Int) = socketChannelMap[id]

}