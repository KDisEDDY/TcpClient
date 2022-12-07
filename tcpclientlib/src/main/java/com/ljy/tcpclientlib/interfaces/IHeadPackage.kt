package com.ljy.tcpclientlib.interfaces

interface IHeadPackage {
    var appFlags: Byte
    var protocolCode: Short
    var protocolVersion: Byte
    var packageBodyLength: Int
    fun reset()

    companion object {
        const val LENGTH = 10
    }
}