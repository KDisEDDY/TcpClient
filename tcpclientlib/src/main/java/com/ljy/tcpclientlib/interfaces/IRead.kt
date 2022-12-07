package com.ljy.tcpclientlib.interfaces

import com.ljy.tcpclientlib.packages.TcpPackage
import java.lang.Exception

interface IRead {
    @Throws(Exception::class)
    fun read(): TcpPackage?
}