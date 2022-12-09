package com.ljy.tcpclientlib.packages

import com.ljy.tcpclientlib.interfaces.IHeadPackage
import com.ljy.tcpclientlib.packages.bodyLinked.AbsBodyLinked
import java.io.Serializable
import java.nio.ByteBuffer

/**
 * 协定的tcp包，包头的长度固定（10 byte）， 包体长度在返回的包头内定义
 */
class TcpPackage : Serializable {
    var headPackage: HeadPackage? = null
    var bodyPackage: BodyPackage? = null
    val packageLength: Int
        get() = IHeadPackage.LENGTH + (bodyPackage?.length ?: 0)

    fun setHead(flags: Byte, code: Short, version: Byte, bodyLength: Int) {
        headPackage?.appFlags = flags
        headPackage?.protocolCode = code
        headPackage?.protocolVersion = version
        headPackage?.packageBodyLength = bodyLength
    }

    fun setBody(linked: AbsBodyLinked?) {
        bodyPackage?.setBodyBuffer(linked)
    }

    fun toByteBuffer(): ByteBuffer {
        val byteBuffer = ByteBuffer.allocate(IHeadPackage.LENGTH + (bodyPackage?.length?:0))
        byteBuffer.put(headPackage?.toByteBuffer()?.flip() as ByteBuffer)
        byteBuffer.position(IHeadPackage.LENGTH)
        if (bodyPackage?.toByteBuffer() != null) {
            byteBuffer.put(bodyPackage?.toByteBuffer()?.flip() as ByteBuffer)
        }
        return byteBuffer
    }

    override fun toString(): String {
        return "head:$headPackage  \tbody:$bodyPackage"
    }

    init {
        headPackage = HeadPackage()
        bodyPackage = BodyPackage()
    }
}