package com.ljy.tcpclientlib.interfaces

interface IAction {
    companion object {
        //数据key
        const val ACTION_DATA = "action_data"

        //socket读线程启动响应
        const val ACTION_READ_THREAD_START = "action_read_thread_start"

        //socket读线程关闭响应
        const val ACTION_READ_THREAD_SHUTDOWN = "action_read_thread_shutdown"

        //socket写线程启动响应
        const val ACTION_WRITE_THREAD_START = "action_write_thread_start"

        //socket写线程关闭响应
        const val ACTION_WRITE_THREAD_SHUTDOWN = "action_write_thread_shutdown"

        //收到推送消息响应
        const val ACTION_READ_RESPONSE = "action_read_response"

        //写给服务器响应
        const val ACTION_WRITE_RESPONSE = "action_writ_response"

        //socket连接服务器成功响应
        const val ACTION_CONNECTION_SUCCESS = "action_connection_success"

        //socket连接服务器失败响应
        const val ACTION_CONNECTION_FAILED = "action_connection_failed"

        //socket与服务器断开连接
        const val ACTION_DISCONNECTION = "action_disconnection"

        //mkt
        const val ACTION_DATA_MKT = "action_data_mkt"
    }
}