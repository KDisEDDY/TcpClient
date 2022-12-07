package com.ljy.tcpclientlib.exceptions

import java.lang.Exception

class LostTcpByteException : Exception {
    constructor() {}
    constructor(detailMessage: String?) : super(detailMessage) {}
    constructor(detailMessage: String?, throwable: Throwable?) : super(detailMessage, throwable) {}
    constructor(throwable: Throwable?) : super(throwable) {}
}