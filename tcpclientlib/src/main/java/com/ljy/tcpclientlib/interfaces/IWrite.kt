package com.ljy.tcpclientlib.interfaces

import java.io.IOException
import java.nio.ByteBuffer

interface IWrite {
    @Throws(IOException::class)
    fun write(byteBuffer: ByteBuffer?): Boolean
}