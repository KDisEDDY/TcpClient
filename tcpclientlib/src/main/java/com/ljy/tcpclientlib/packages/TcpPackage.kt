package com.ljy.tcpclientlib.packages

import com.ljy.tcpclientlib.interfaces.IHeadPackage
import com.ljy.tcpclientlib.packages.bodyLinked.AbsBodyLinked
import java.io.Serializable
import java.nio.ByteBuffer

class TcpPackage : Serializable {
    var headPackage: HeadPackage? = null
    var bodyPackage: BodyPackage? = null
    var mkt: String? = null
    val packageLength: Int
        get() = IHeadPackage.LENGTH + (bodyPackage?.length ?: 0)

    fun setHead(flags: Byte, code: Short, version: Byte, bodyLength: Int) {
        headPackage?.appFlags = flags
        headPackage?.protocolCode = code
        headPackage?.protocolVersion = version
        headPackage?.packageBodyLength = bodyLength
    }

    fun setBody(linked: AbsBodyLinked?) {
        bodyPackage!!.setBodyBuffer(linked)
    }

    fun toByteBuffer(): ByteBuffer {
        val byteBuffer = ByteBuffer.allocate(IHeadPackage.LENGTH + bodyPackage!!.length)
        byteBuffer.put(headPackage?.toByteBuffer()?.flip() as ByteBuffer)
        byteBuffer.position(IHeadPackage.LENGTH)
        if (bodyPackage!!.toByteBuffer() != null) {
            byteBuffer.put(bodyPackage!!.toByteBuffer()!!.flip() as ByteBuffer)
        }
        return byteBuffer
    }

    override fun toString(): String {
        var result = ""
        result = "head:" + headPackage + "  \tbody:" + bodyPackage + "mkt" + mkt
        return result
    }

    init {
        headPackage = HeadPackage()
        bodyPackage = BodyPackage()
    }
}