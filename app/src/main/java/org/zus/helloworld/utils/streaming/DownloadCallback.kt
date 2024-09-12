package org.zus.helloworld.utils.streaming

internal abstract class DownloadCallback {
    abstract fun completed(data: ByteArray?)
    abstract fun inProgress(data: ByteArray?)
    abstract fun error(e: Exception?)
}