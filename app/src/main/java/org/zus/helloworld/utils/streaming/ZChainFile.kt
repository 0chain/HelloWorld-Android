package org.zus.helloworld.utils.streaming

abstract class ZChainFile {
    var tempPath: String? = null
    var fileTotalBytes: Long? = null
    var numBlocks: Long? = null
    var mimeType = "video/mp4"
    var lookupHash: String? = null
    var chunkSize: Long = 0
}