package com.ljy.tcpclientlib.exceptions

import java.io.IOException

class OpenChannelException : IOException {
    constructor() : super() {}

    constructor(cause: Throwable?) : super(cause) {
    }

    constructor(detailMessage: String?) : super(detailMessage) {}

    constructor(message: String?, cause: Throwable?) : super(message, cause) {
    }
}