package com.ljy.tcpclientlib.interfaces

/**
 * 解除连接服务器
 */
interface IDisconnect {

    fun disconnectAll()

    fun disconnect(channelId: Int)
}