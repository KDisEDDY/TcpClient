package com.ljy.tcpclientlib.interfaces

import com.ljy.tcpclientlib.packages.TcpPackage
import java.net.InetSocketAddress

/**
 * @author Eddy.Liu
 * @Date 2022/12/9
 * @Description
 **/
interface IPackageReceiver {
    fun onTcpPackageResponse(inetSocketAddress: InetSocketAddress, tcpPackage: TcpPackage, ops: Int)
}