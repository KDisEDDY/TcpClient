package com.ljy.tcpclientlib.interfaces

import java.lang.Exception

/**
 * 连接服务器
 */
interface IConnection {
    @Throws(Exception::class)
    fun connection(ip: String?, port: Int)
}