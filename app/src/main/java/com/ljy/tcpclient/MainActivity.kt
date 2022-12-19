package com.ljy.tcpclient

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.ljy.tcpclient.ui.theme.TcpClientTheme
import com.ljy.tcpclientlib.Connection
import com.ljy.tcpclientlib.TcpClient
import com.ljy.tcpclientlib.packages.TcpPackage
import com.ljy.tcpclientlib.receiver.ResponseHandler

class MainActivity : ComponentActivity() {

    lateinit var tcpClient: TcpClient
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        buildTcpClient()
        setContent {
            TcpClientTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    ButtonLayout()
                }
            }
        }
    }

    private fun buildTcpClient() {
        tcpClient = TcpClient(this, 3)
    }

    @Preview
    @Composable
    fun ButtonLayout() {
        Row {
            TextButton(onClick = {
                tcpClient.connection(Connection("10.9.30.63", 8007, 8007, object : ResponseHandler{
                    override fun onWriteResponse(tcpPackage: TcpPackage) {

                    }

                    override fun onReadResponse(tcpPackage: TcpPackage) {

                    }

                }))
            }) {
                Text("connect to server")
            }

            TextButton(onClick = {
                tcpClient.disconnect()
            }) {
                Text("disconnect to server")

            }
        }
    }
}