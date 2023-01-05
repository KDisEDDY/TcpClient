package com.ljy.tcpclientlib.packages

import com.ljy.tcpclientlib.interfaces.IHeadPackage
import java.io.Serializable
import java.nio.ByteBuffer

class HeadPackage : IHeadPackage, Serializable {
    private var headBuffer: ByteBuffer? = null

    constructor() {
        headBuffer = ByteBuffer.allocate(IHeadPackage.LENGTH)
    }

    constructor(byteBuffer: ByteBuffer) {
        headBuffer = byteBuffer
    }

    override var appFlags: Byte
        get() {
            synchronized(headBuffer!!) {
                headBuffer!!.position(0)
                return headBuffer!!.get()
            }
        }
        set(flags) {
            synchronized(headBuffer!!) {
                headBuffer!!.position(0)
                headBuffer!!.put(flags)
            }
        }
    override var protocolCode: Short
        get() {
            synchronized(headBuffer!!) {
                headBuffer!!.position(1)
                return headBuffer!!.getShort()
            }
        }
        set(protocolCode) {
            synchronized(headBuffer!!) {
                headBuffer!!.position(1)
                headBuffer!!.putShort(protocolCode)
            }
        }
    override var protocolVersion: Byte
        get() {
            synchronized(headBuffer!!) {
                headBuffer!!.position(3)
                return headBuffer!!.get()
            }
        }
        set(version) {
            synchronized(headBuffer!!) {
                headBuffer!!.position(3)
                headBuffer!!.put(version)
            }
        }
    override var packageBodyLength: Int
        get() {
            synchronized(headBuffer!!) {
                headBuffer!!.position(4)
                return headBuffer!!.int
            }
        }
        set(length) {
            synchronized(headBuffer!!) {
                headBuffer!!.position(4)
                headBuffer!!.putInt(length)
            }
        }

    override fun reset() {
        headBuffer = null
    }

    fun toByteBuffer(): ByteBuffer? {
        return headBuffer
    }

    override fun toString(): String {
        var result = ""
        result = "{app标志:" + appFlags + "}," +
                "{协议代码:" + protocolCode + "}," +
                "{协议版本:" + protocolVersion + "}," +
                "{包体长度:" + packageBodyLength + "}"
        return result
    }
}