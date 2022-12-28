package com.ljy.tcpclient

import android.os.Bundle

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import com.ljy.tcpclient.ui.theme.TcpClientTheme
import com.ljy.tcpclientlib.Connection
import com.ljy.tcpclientlib.TcpClient
import com.ljy.tcpclientlib.packages.TcpPackage
import com.ljy.tcpclientlib.worker.ResponseHandler

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
        tcpClient = TcpClient(this)
    }

    @Preview
    @Composable
    fun ButtonLayout() {
        var text by remember {
            mutableStateOf("10.9.30.63")
        }
        ConstraintLayout {
            val (ipText, connectLayout, disconnectLayout) = createRefs()
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
            Row(modifier = Modifier.constrainAs(connectLayout) {
                top.linkTo(ipText.bottom, margin = 10.dp)
            }) {
                var channelId by remember {
                    mutableStateOf("8007")
                }
                ChannelIdTextField(channelId, onIdChanged = {channelId = it})
                TextButton(onClick = {
                    tcpClient.connection(Connection(text, 8007, channelId.toIntOrNull() ?: 0, object : ResponseHandler {
                        override fun onWriteResponse(tcpPackage: TcpPackage) {

                        }

                        override fun onReadResponse(tcpPackage: TcpPackage) {

                        }

                    }))
                }) {
                    Text("connect to server")
                }
            }

            Row(modifier = Modifier.constrainAs(disconnectLayout) {
                top.linkTo(connectLayout.bottom, margin = 10.dp)
            }) {
                var channelId by remember {
                    mutableStateOf("8007")
                }
                ChannelIdTextField(channelId, onIdChanged = {channelId = it})

                TextButton(onClick = {
                    val id = channelId.toIntOrNull() ?: 0
                    if (id == 0) {
                        tcpClient.disconnectAll()
                    } else {
                        tcpClient.disconnect(id)
                    }
                }) {
                    Text("disconnect to server")
                }


            }
        }
        }

    @Composable
    fun ChannelIdTextField(channelId: String, onIdChanged: (String) -> Unit) {
        TextField(value = channelId, onValueChange = onIdChanged,
            label = {
                Text(text = "channelId")
            })
    }
    }