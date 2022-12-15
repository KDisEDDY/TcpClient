package com.ljy.tcpclientlib

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import java.net.InetSocketAddress

/**
 * @author Eddy.Liu
 * @Date 2022/12/8
 * @Description
 **/
object NetUtils {

    fun netIsAvailable(context: Context): Boolean {
        val manager = context
            .getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return false
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            manager.getNetworkCapabilities(manager.activeNetwork)?.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) ?: false
        } else {
            !(manager.activeNetworkInfo == null || manager.activeNetworkInfo?.isAvailable == false)
        }
    }

    fun hashKey4INetSocketAddress(inetSocketAddress: InetSocketAddress) = (inetSocketAddress.hostName + inetSocketAddress.port).hashCode()

    fun hashKey4INetSocketAddress(hostName: String?, port: Int) = (hostName + port).hashCode()
}