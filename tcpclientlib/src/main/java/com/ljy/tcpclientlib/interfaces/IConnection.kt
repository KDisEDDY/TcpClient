package com.ljy.tcpclientlib.interfaces

import com.ljy.tcpclientlib.Connection
import java.lang.Exception

/**
 * 连接服务器
 */
interface IConnection {
    @Throws(Exception::class)
    fun connection(connection: Connection)
}