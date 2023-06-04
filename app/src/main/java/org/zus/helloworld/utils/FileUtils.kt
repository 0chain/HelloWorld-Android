package org.zus.helloworld.utils

import android.content.Context
import android.net.Uri
import java.io.*

@Throws(IOException::class)
fun from(context: Context, uri: Uri?, fileName: String): File {
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