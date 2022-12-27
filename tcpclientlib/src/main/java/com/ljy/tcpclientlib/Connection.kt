package com.ljy.tcpclientlib

import com.ljy.tcpclientlib.worker.ResponseHandler

/**
 * @author Eddy.Liu
 * @Date 2022/12/15
 * @Description
 **/
data class Connection(val ip: String?, val port: Int, val channelId: Int, val responseHandler: ResponseHandler?)