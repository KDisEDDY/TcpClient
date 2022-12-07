package com.ljy.tcpclientlib.packages

import com.ljy.tcpclientlib.interfaces.IBodyPackage
import com.ljy.tcpclientlib.packages.bodyLinked.AbsBodyLinked
import java.io.Serializable
import java.io.UnsupportedEncodingException
import java.lang.Exception
import java.nio.ByteBuffer

class BodyPackage : IBodyPackage, Serializable {
    private var bodyBuffer: ByteBuffer? = null

    constructor(bodyBuffer: ByteBuffer?) {
        this.bodyBuffer = bodyBuffer
    }

    constructor() {}

    protected fun initBuffer(length: Int) {
        if (bodyBuffer == null) {
            bodyBuffer = ByteBuffer.allocate(length)
        }
    }

    override fun reset() {
        bodyBuffer = null
    }

    override val length: Int
        get() = if (bodyBuffer != null) {
            bodyBuffer!!.limit()
        } else 0

    fun setBodyBuffer(linked: AbsBodyLinked?) {
        var linked = linked
        reset()
        if (linked == null) return
        initBuffer(linked.totalLength)
        try {
            synchronized(bodyBuffer!!) {
                while (linked != null && linked!!.link(bodyBuffer)) {
                    linked = linked!!.next()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun toByteBuffer(): ByteBuffer? {
        return bodyBuffer
    }

    override fun toString(): String {
        var result: String? = null
        result = try {
            if (bodyBuffer == null) {
                ""
            } else {
                String(bodyBuffer!!.array(), Charsets.UTF_8)
            }
        } catch (e: UnsupportedEncodingException) {
            "UnsupportedEncodingException"
        }
        return result!!
    }
}