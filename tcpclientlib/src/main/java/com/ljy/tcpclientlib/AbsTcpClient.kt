package com.ljy.tcpclientlib

import android.content.Context
import com.ljy.tcpclientlib.exceptions.OpenChannelException
import com.ljy.tcpclientlib.interfaces.IConnection
import com.ljy.tcpclientlib.interfaces.IDisconnect
import java.io.IOException
import java.net.InetSocketAddress
import java.nio.channels.SocketChannel

/**
 * @author Eddy.Liu
 * @Date 2022/12/7
 * @Description client的接口
 **/
abstract class AbsTcpClient(val context: Context) : IConnection, IDisconnect {

    private var isUnexpectedDisConnection = false

    var inetSocketAddressMap = mutableMapOf<Int, InetSocketAddress>()

    private val socketChannelMap = mutableMapOf<Int, SocketChannel>()

    @Throws(OpenChannelException::class)
    protected open fun openChannel(ip: String?, port: Int): Int {
        isUnexpectedDisConnection = false
        val inetSocketAddress = InetSocketAddress(ip, port)
        val hashKey = NetUtils.hashKey4INetSocketAddress(inetSocketAddress)
        inetSocketAddressMap[hashKey] = inetSocketAddress
        try {
            val socketChannel = SocketChannel.open()
            socketChannel?.configureBlocking(false)
            socketChannelMap[hashKey] = socketChannel
        } catch (e: IOException) {
            throw OpenChannelException(e)
        }
        return hashKey
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