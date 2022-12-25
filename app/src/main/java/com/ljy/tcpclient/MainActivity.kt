package com.ljy.tcpclient

import android.os.Bundle

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
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
        var text by remember {
            mutableStateOf("192.168.31.6")
        }
        ConstraintLayout {
            val (ipText, buttonLayout) = createRefs()
            TextField(value = text, onValueChange = {
                text = it
            },
                label = {
                    Text(text = "ip")
                },
                modifier = Modifier.constrainAs(ipText) {
                    top.linkTo(parent.top)
                }
            )
            Row(modifier = Modifier.constrainAs(buttonLayout) {
                top.linkTo(ipText.bottom, margin = 10.dp)
            }) {
                TextButton(onClick = {
                    tcpClient.connection(Connection(text, 8007, 8007, object : ResponseHandler{
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
    }