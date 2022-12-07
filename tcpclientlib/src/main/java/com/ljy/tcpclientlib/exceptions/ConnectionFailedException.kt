package com.ljy.tcpclientlib.exceptions

import java.io.IOException

class ConnectionFailedException : IOException {
    constructor() {}
    constructor(detailMessage: String?) : super(detailMessage) {}
    constructor(detailMessage: String?, throwable: Throwable?) : super(detailMessage, throwable) {}
    constructor(throwable: Throwable?) : super(throwable) {}
}