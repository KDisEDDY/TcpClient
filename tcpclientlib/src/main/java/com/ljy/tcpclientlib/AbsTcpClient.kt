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

    var inetSocketAddress: InetSocketAddress? = null

    protected var mSocketChannel: SocketChannel? = null

    @Throws(OpenChannelException::class)
    protected open fun openChannel(ip: String?, port: Int) {
        isUnexpectedDisConnection = false
        inetSocketAddress = InetSocketAddress(ip, port)
        try {
            mSocketChannel = SocketChannel.open()
            mSocketChannel?.configureBlocking(false)
        } catch (e: IOException) {
            throw OpenChannelException(e)
        }
    }

    @Throws(IOException::class)
    protected open fun connect() {
        if (mSocketChannel == null || inetSocketAddress == null) {
            throw IOException("channel need open first")
        }
        mSocketChannel?.connect(inetSocketAddress)
    }

}