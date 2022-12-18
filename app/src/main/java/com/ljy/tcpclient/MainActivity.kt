package com.ljy.tcpclient

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.ljy.tcpclient.ui.theme.TcpClientTheme
import com.ljy.tcpclientlib.Connection
import com.ljy.tcpclientlib.TcpClient
import com.ljy.tcpclientlib.packages.TcpPackage
import com.ljy.tcpclientlib.receiver.ResponseHandler

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TcpClientTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    Greeting("Android")
                }
            }
        }
    }

    fun connectToServer() {
        TcpClient(this, 3).apply {
            connection(Connection("127.0.0.1", 1004, 1004, object : ResponseHandler{
                override fun onWriteResponse(tcpPackage: TcpPackage) {

                }

                override fun onReadResponse(tcpPackage: TcpPackage) {

                }

            }))
        }
    }
}

@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
}

@Composable
fun DefaultPreview() {
    TcpClientTheme {
        Greeting("Android")
    }
}