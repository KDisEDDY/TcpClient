package com.ljy.tcpclientlib.packages.bodyLinked

import com.ljy.tcpclientlib.packages.bodyLinked.AbsBodyLinked
import java.lang.Exception
import java.nio.ByteBuffer

abstract class AbsBodyLinked {
    protected var next: AbsBodyLinked? = null
    protected var thisLinkedLength = 0

    constructor(next: AbsBodyLinked?, thisLinkedLength: Int) {
        this.next = next
        this.thisLinkedLength = thisLinkedLength
    }

    constructor(thisLinkedLength: Int) {
        this.thisLinkedLength = thisLinkedLength
    }

    fun setNextLinked(link: AbsBodyLinked?) {
        next = link
    }

    operator fun next(): AbsBodyLinked? {
        return next
    }

    /**
     * 连接下一个数据区
     * @param byteBuffer
     * @return 是否有下一个，true有，false没有
     */
    @Throws(Exception::class)
    abstract fun link(byteBuffer: ByteBuffer?): Boolean
    val totalLength: Int
        get() {
            var result = thisLinkedLength
            if (next != null) {
                result += next!!.totalLength
            }
            return result
        }
}