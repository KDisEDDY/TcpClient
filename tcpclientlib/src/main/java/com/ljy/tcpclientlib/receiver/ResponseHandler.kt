package com.ljy.tcpclientlib.receiver

import com.ljy.tcpclientlib.packages.TcpPackage

/**
 * @author Eddy.Liu
 * @Date 2022/12/9
 * @Description
 **/
interface ResponseHandler {

    /**
     * 这个是已写入通道的数据，并非在reponse方法里面做写入通道操作
     */
    fun onWriteResponse(tcpPackage: TcpPackage)

    /**
     * 从通道里面接收到的数据，在这里分到外部做解析
     */
    fun onReadResponse(tcpPackage: TcpPackage)

}