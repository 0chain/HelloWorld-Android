package org.zus.helloworld.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.core.content.FileProvider
import java.io.*
import java.nio.file.Files

@Throws(IOException::class)
fun from(context: Context, uri: Uri?, fileName: String?): File {
    val inputStream = context.contentResolver.openInputStream(uri!!)
    val privateFile = File(context.filesDir, fileName)
    var out: FileOutputStream? = null
    try {
        out = FileOutputStream(privateFile)
    } catch (e: FileNotFoundException) {
        e.printStackTrace()
    }
    if (inputStream != null) {
        out?.let { copy(inputStream, it) }
        inputStream.close()
    }
    out?.close()
    return privateFile
}
@Throws(IOException::class)
private fun copy(input: InputStream, output: FileOutputStream): Long {
    var count: Long = 0
    var n: Int
    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
    val EOF = -1
    while (EOF != input.read(buffer).also {
            n = it
        }) {
        output.write(buffer, 0, n)
        count += n.toLong()
    }
    return count
}

@Throws(IOException::class)
fun getThumbnail(context: Context, file: File, thumbnailRoot: String, fileName: String): String? {
    return try {
        var mimeType: String? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mimeType = Files.probeContentType(file.toPath())
        }
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        options.inSampleSize = 6
        var inputStream = FileInputStream(file)
        if (mimeType != null && mimeType.startsWith("video")) {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(file.absolutePath)
            val frame = retriever.frameAtTime
            options.outWidth = frame!!.width
            options.outHeight = frame.height
        } else {
            BitmapFactory.decodeStream(inputStream, null, options)
        }
        inputStream.close()
        val REQUIRED_SIZE = 75
        var scale = 1
        while (options.outWidth / scale / 2 >= REQUIRED_SIZE &&
            options.outHeight / scale / 2 >= REQUIRED_SIZE
        ) {
            scale *= 2
        }
        val options1 = BitmapFactory.Options()
        options1.inSampleSize = scale
        inputStream = FileInputStream(file)
        var selectedBitmap: Bitmap? = null
        selectedBitmap = if (mimeType != null && mimeType.startsWith("video")) {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(file.absolutePath)
            val frame = retriever.frameAtTime
            Bitmap.createScaledBitmap(
                frame!!,
                options.outWidth / scale,
                options.outHeight / scale,
                false
            )
        } else {
            BitmapFactory.decodeStream(inputStream, null, options1)
        }
        inputStream.close()

        // Compress the bitmap until its size is within the desired limit (1MB)
        /*val MAX_SIZE = 1024 * 1024 // 1MB
        var quality = 90 // Initial compression quality
        var outputStream: FileOutputStream? = null
        var thumbnailFile: File? = null*/
        if (!makeDirectories(
                context,
                thumbnailRoot.substring(1, thumbnailRoot.length - 1)
            )
        ) {
            throw java.lang.RuntimeException("Failed to create directory")
        }
        var thumbnailFile = File(context.filesDir, thumbnailRoot.substring(1) + fileName)
        var outputStream = FileOutputStream(thumbnailFile)
        selectedBitmap!!.compress(Bitmap.CompressFormat.JPEG, 50, outputStream)
        return if (thumbnailFile.exists()) thumbnailFile.absolutePath else ""
        /*while (selectedBitmap != null && getBitmapSize(selectedBitmap) > MAX_SIZE && quality > 0) {
            outputStream?.close() // Close previous stream, if any
            quality -= 10 // Reduce the compression quality
            thumbnailFile = File(context.filesDir, thumbnailRoot.substring(1) + fileName)
            outputStream = FileOutputStream(thumbnailFile)

            // Determine the compression format based on the mime type
            val compressFormat = if (mimeType != null && mimeType.startsWith("image/png")) {
                Bitmap.CompressFormat.PNG
            } else {
                Bitmap.CompressFormat.JPEG
            }

            selectedBitmap.compress(compressFormat, quality, outputStream)
        }
        outputStream?.close() // Close the stream for the final compressed bitmap

        if (thumbnailFile != null && thumbnailFile.exists()) thumbnailFile.absolutePath else ""*/
    } catch (e: Exception) {
        ""
    }
}

private fun getBitmapSize(bitmap: Bitmap): Int {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
        return bitmap.allocationByteCount
    }
    return bitmap.byteCount
}


fun makeDirectories(context: Context, root: String): Boolean {
    if (root.isEmpty()) return true
    return if (File(context.filesDir, root).exists()) {
        true
    } else {
        if (makeDirectories(
                context,
                root.substring(0, if (root.lastIndexOf("/") == -1) 0 else root.lastIndexOf("/"))
            )
        ) File(
            context.filesDir,
            root
        ).mkdir() else throw RuntimeException("Failed to create directory")
    }
}
fun copyFileToDownloads(context: Context, files: org.zus.helloworld.data.Files): Boolean {
    return try {
        val fileAtInternalStorage: File = File(files.getAndroidPath())
        val contentResolver = context.contentResolver
        val uriForFile = FileProvider.getUriForFile(
            context,
            context.packageName + ".files.provider",
            fileAtInternalStorage
        )
        val downloadsDir =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val originalFileName: String? = files.name
        var fileName = originalFileName
        var fileCount = 1
        val extensionIndex = originalFileName!!.lastIndexOf(".")
        var extension = ""
        if (extensionIndex != -1) {
            extension = originalFileName.substring(extensionIndex)
            fileName = originalFileName.substring(0, extensionIndex)
        }
        var destinyFile = File(downloadsDir, fileName + extension)
        while (destinyFile.exists()) {
            // Append (1), (2), etc. to file name if file with same name exists
            fileName = originalFileName.substring(0, extensionIndex) + "(" + fileCount + ")"
            fileCount++
            destinyFile = File(downloadsDir, fileName + extension)
        }
        val inputStream = contentResolver.openInputStream(uriForFile)
        var out: FileOutputStream? = null
        try {
            out = FileOutputStream(destinyFile)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
        if (inputStream != null) {
            out?.let { copy(inputStream, it) }
            inputStream.close()
        }
        out?.close()
        true
    } catch (e: java.lang.Exception) {
        //TODO implement another way to copy file to downloads, exception not thrown for Android 12
        false
    }
}
