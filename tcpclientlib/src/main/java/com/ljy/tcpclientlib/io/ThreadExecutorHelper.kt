package com.ljy.tcpclientlib.io

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * @author Eddy.Liu
 * @Date 2022/12/12
 * @Description IO 操作用单独的线程进行处理
 **/
class ThreadExecutorHelper {

    private val executorService: ExecutorService = Executors.newCachedThreadPool()

//    fun execute(nio: NIO, absReceiver: AbsReceiver, inetSocketAddress: InetSocketAddress, Ops: Int) {
//        executorService.execute {
//            nio.read()?.let {
//                // 给到外部的接收器
//                absReceiver.onTcpPackageResponse(inetSocketAddress, it, Ops)
//            }
//        }
//    }



}