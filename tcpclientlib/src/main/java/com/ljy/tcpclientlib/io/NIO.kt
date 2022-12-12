package com.ljy.tcpclientlib.io

import android.os.SystemClock
import android.util.Log
import com.ljy.tcpclientlib.Constant
import com.ljy.tcpclientlib.interfaces.IRead
import com.ljy.tcpclientlib.interfaces.IWrite
import com.ljy.tcpclientlib.packages.TcpPackage
import com.ljy.tcpclientlib.packages.HeadPackage
import com.ljy.tcpclientlib.packages.BodyPackage
import com.ljy.tcpclientlib.exceptions.LostTcpByteException
import com.ljy.tcpclientlib.interfaces.IHeadPackage.Companion.LENGTH
import java.io.IOException
import java.lang.Exception
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.SocketChannel

/**
 * 读写IO， 通过ByteBuffer 来做缓冲区，
 */
class NIO(socketChannel: SocketChannel?) : IRead, IWrite {
    private var socketChannel: SocketChannel? = null
    @Throws(IOException::class)
    override fun write(byteBuffer: ByteBuffer?): Boolean {
        if (socketChannel?.isConnected == false || null == byteBuffer) {
            return false
        }
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN)
        var count = 0
        try {
            while (byteBuffer.hasRemaining()) {
                val hasWriteCount = socketChannel!!.write(byteBuffer)
                if (hasWriteCount > 0) {
                    count += hasWriteCount
                }
            }
            Log.i(TAG, "NIO write is finish,writeCount:$count Bytes")
        } catch (e: Exception) {
            Log.i(
                TAG,
                "write is abort, allready write " + count + " Bytes. failed cause:" + e.message
            )
            throw e
        }
        return true
    }

    @Throws(Exception::class)
    override fun read(): TcpPackage? {
        if (socketChannel?.isConnected == false) {
            return null
        }
        var tcpPackage: TcpPackage? = null
        var headPackage: HeadPackage? = null
        var bodylength = 0
        try {
            val headBuffer = ByteBuffer.allocate(LENGTH)
            read(headBuffer, LENGTH)
            headPackage = HeadPackage(headBuffer)
            bodylength = headPackage.packageBodyLength
        } catch (e: Exception) {
            Log.i(TAG, "read thread was droped package in analytic headpackage cause:" + e.message)
            throw e
        }
        try {
            var bodyPackage: BodyPackage? = null
            if (bodylength > 0) {
                val bodyBuffer = ByteBuffer.allocate(bodylength)
                read(bodyBuffer, bodylength)
                bodyPackage = BodyPackage(bodyBuffer)
            }
            if (headPackage != null) { //被丢弃可能会导致null
                tcpPackage = TcpPackage()
                tcpPackage.headPackage = headPackage
                if (bodyPackage != null) {
                    tcpPackage.bodyPackage = bodyPackage
                }
            }
            Log.i(TAG, "NIO read is finish,readCount:" + tcpPackage?.packageLength + " Bytes")
        } catch (e: Exception) {
            Log.i(TAG, "read thread was droped package in analytic bodypackage cause:" + e.message)
            throw e
        }
        return tcpPackage
    }

    @Throws(Exception::class)
    private fun read(byteBuffer: ByteBuffer, length: Int) {
        var count = 0
        var readBeginMills = SystemClock.elapsedRealtime()
        while (count < length) {
            try {
                val readCount = socketChannel!!.read(byteBuffer)
                Log.i(TAG, "readCount = $readCount")
                val nowMills = SystemClock.elapsedRealtime()
                if (readCount > 0) {
                    count += readCount
                    readBeginMills = nowMills
                }
                //1.readCount为-1时是连接断开了，直接报错重连
                //2.如果读取数据超过了20s也报错重连
                if (nowMills - readBeginMills >= 20000 || readCount == -1) {
                    throw LostTcpByteException("byte lost exception,need to shutdown and reconnection")
                }
            } catch (e: Exception) {
                Log.i(
                    TAG,
                    "read is abort, allready read " + count + " Bytes. failed cause:" + e.message
                )
                throw e
            }
        }
    }

    companion object {
        private const val TAG = "${Constant.CLIENT_LOG}_NIO"
    }

    init {
        this.socketChannel = socketChannel
    }
}