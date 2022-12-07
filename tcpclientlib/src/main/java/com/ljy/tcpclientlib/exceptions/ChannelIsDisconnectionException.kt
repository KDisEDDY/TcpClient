package com.ljy.tcpclientlib.exceptions

import java.lang.Exception

class ChannelIsDisconnectionException : Exception {
    constructor() {}
    constructor(detailMessage: String?) : super(detailMessage) {}
    constructor(detailMessage: String?, throwable: Throwable?) : super(detailMessage, throwable) {}
    constructor(throwable: Throwable?) : super(throwable) {}
}