package org.zus.helloworld.utils.streaming

import com.google.android.exoplayer2.util.Log
import zbox.StatusCallbackMocked
import java.io.*


class ZBoxCallback internal constructor(private val localPath: String, callback: DownloadCallback) :
    StatusCallbackMocked {
    private val callback: DownloadCallback?

    init {
        this.callback = callback
    }

    override fun commitMetaCompleted(s: String, s1: String, e: Exception) {}
    override fun completed(
        allocationId: String,
        filePath: String,
        filename: String,
        mimetype: String,
        size: Long,
        op: Long
    ) {
        val file = File(localPath)
        val bytes = ByteArray(file.length().toInt())
        try {
            val buf = BufferedInputStream(FileInputStream(file))
            buf.read(bytes, 0, bytes.size)
            buf.close()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        // delete file
        file.delete()
        Log.d("debug", "completed " + bytes.size)
        if (callback != null) {
            callback.completed(bytes)
        }
    }

    override fun error(s: String, s1: String, l: Long, e: Exception) {
        Log.e("debug", "error", e)
        if (callback != null) {
            callback.error(e)
        }
    }

    override fun inProgress(s: String, s1: String, l: Long, l1: Long, data: ByteArray) {
        if (callback != null) {
            callback.inProgress(data)
        }
    }

    override fun repairCompleted(l: Long) {}
    override fun started(s: String, s1: String, l: Long, l1: Long) {
        Log.d("debug", "started $s")
    }
}