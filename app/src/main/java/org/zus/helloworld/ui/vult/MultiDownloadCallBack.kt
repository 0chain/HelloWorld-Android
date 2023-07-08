package org.zus.helloworld.ui.vult

import android.util.Log
import android.widget.Toast
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.zus.helloworld.data.Files
import zbox.StatusCallbackMocked
import java.util.function.Predicate
import java.util.stream.Collectors

class MultiDownloadCallBack(files: List<Files>, bindingRoot: SwipeRefreshLayout) :
    StatusCallbackMocked {
    var uploadingFiles: List<Files>
    var root: SwipeRefreshLayout

    init {
        uploadingFiles = files
        root = bindingRoot
    }

    override fun commitMetaCompleted(s: String, s1: String, e: Exception) {}
    override fun completed(
        allocationId: String,
        remotePath: String,
        fileName: String,
        mimetype: String,
        size: Long,
        op: Long
    ) {
        Log.i(
            TAG_VULT,
            "$fileName downloaded"
        )
        CoroutineScope(Dispatchers.Main).launch {
            val snackbar = Snackbar
                .make(
                    root,
                    "$fileName downloaded to downloads directory",
                    Snackbar.LENGTH_SHORT
                )
            snackbar.show()
        }
    }

    override fun error(allocationId: String, remotePath: String, op: Long, e: Exception) {
        e.printStackTrace()
        val file: Files =
            uploadingFiles.stream().filter(Predicate<Files> { files: Files ->
                files.remotePath == remotePath
            }).collect(Collectors.toList<Files>())[0]
        CoroutineScope(Dispatchers.Main).launch {
            if (e.message?.contains("Local file already exists") == true) {
                val snackbar = Snackbar
                    .make(
                        root,
                        "${file.name} already downloaded",
                        Snackbar.LENGTH_SHORT
                    )
                snackbar.show()
            } else {
                val snackbar = Snackbar
                    .make(
                        root,
                        "Failed to download ${file.name}",
                        Snackbar.LENGTH_SHORT
                    )
                snackbar.show()
            }
        }
    }

    override fun inProgress(
        allocationId: String,
        remotePath: String,
        op: Long,
        completedBytes: Long,
        data: ByteArray
    ) {
    }

    override fun repairCompleted(l: Long) {}

    override fun started(allocationId: String, remotePath: String, op: Long, totalBytes: Long) {
        val file: Files =
            uploadingFiles.stream().filter(Predicate<Files> { files: Files ->
                files.remotePath == remotePath
            }).collect(Collectors.toList<Files>())[0]
    }
}