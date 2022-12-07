package com.ljy.tcpclientlib.interfaces

import android.content.Context
import com.ljy.tcpclientlib.packages.TcpPackage

interface ISocketResponseHandler {
    fun onSocketIOThreadStart(context: Context?, mkt: String?)
    fun onSocketIOThreadShutdown(context: Context?, mkt: String?)
    fun onSocketDisconnection(context: Context?, mkt: String?, isUnexpectedDisconnection: Boolean)
    fun onSocketConnectionSuccess(context: Context?, mkt: String?)
    fun onSocketConnectionFailed(context: Context?, mkt: String?)
    fun onSocketReadResponse(context: Context?, mkt: String?, tcpPackage: TcpPackage?)
    fun onSocketWriteResponse(context: Context?, mkt: String?, tcpPackage: TcpPackage?)
    fun onSocketReadResponseHK(context: Context?, mkt: String?, tcpPackage: TcpPackage?)
    fun onSocketReadResponseUS(context: Context?, mkt: String?, tcpPackage: TcpPackage?)
    fun onSocketReadResponseML(context: Context?, mkt: String?, tcpPackage: TcpPackage?)
}