package com.ljy.tcpclientlib.receiver

import android.util.Log
import com.ljy.tcpclientlib.Constant
import com.ljy.tcpclientlib.interfaces.IPackageReceiver
import com.ljy.tcpclientlib.packages.TcpPackage
import java.net.InetSocketAddress
import java.nio.channels.SelectionKey


/**
 * @author Eddy.Liu
 * @Date 2022/12/9
 * @Description 接收到回包数据，给到外部做分发
 **/
class AbsReceiver: IPackageReceiver {

    companion object {
        const val TAG = "${Constant.CLIENT_LOG}_AbsReceiver"
    }

    private val map = mutableMapOf<Int, ResponseHandler>()

    fun registerReceiver(inetSocketAddress: InetSocketAddress, responseHandler: ResponseHandler) {
        // 这里考虑到的是，回调给外部的responseHandler需要有一个指定的接收者，这里根据连接的inetAddress，也就是对应的 (ip + port).hashcode 来作为接收者的唯一标识
        val receiverId = (inetSocketAddress.hostName + inetSocketAddress.port).hashCode()
        if (map[receiverId] == null) {
            map[receiverId] = responseHandler
        }
    }

    override fun onTcpPackageResponse(inetSocketAddress: InetSocketAddress, tcpPackage: TcpPackage, ops: Int) {
        when (ops) {
            SelectionKey.OP_READ -> {
                map[(inetSocketAddress.hostName + inetSocketAddress.port).hashCode()]?.onReadResponse(tcpPackage)
            }
            // 写操作要斟酌一下，是否通过receiver
            SelectionKey.OP_WRITE -> {
                map[(inetSocketAddress.hostName + inetSocketAddress.port).hashCode()]?.onWriteResponse(tcpPackage)
            }
            else -> {
                Log.d(TAG, "the ops is wrong PLEASE CHECK IT!")
            }
        }
    }

}