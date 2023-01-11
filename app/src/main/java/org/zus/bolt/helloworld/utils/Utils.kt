package org.zus.bolt.helloworld.utils

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import androidx.core.database.getStringOrNull
import com.google.gson.Gson
import org.json.JSONObject
import org.zus.bolt.helloworld.models.bolt.WalletModel
import java.io.File
import java.io.FileNotFoundException
import java.text.SimpleDateFormat
import java.util.*

class Utils(var applicationContext: Context) {
    companion object {
        fun getDateTime(s: Long): String? {
            try {
                val sdf = SimpleDateFormat("yyyy/MM/dd 'at' HH:mm:ss a", Locale.ENGLISH)
                val netDate = Date(s)
                return sdf.format(netDate)
            } catch (e: Exception) {
                return e.toString()
            }
        }

        fun getStorage(size: Int): String {
            val gb = 1024 * 1024 * 1024
            val mb = 1024 * 1024
            val kb = 1024
            return when {
                size > gb -> {
                    "${size / gb} GB"
                }
                size > mb -> {
                    "${size / mb} MB"
                }
                size > kb -> {
                    "${size / kb} KB"
                }
                else -> {
                    "$size B"
                }
            }
        }

        fun getStorage(size: Long): String {
            val gb = 1024 * 1024 * 1024
            val mb = 1024 * 1024
            val kb = 1024
            return when {
                size > gb -> {
                    "${size / gb} GB"
                }
                size > mb -> {
                    "${size / mb} MB"
                }
                size > kb -> {
                    "${size / kb} KB"
                }
                else -> {
                    "$size B"
                }
            }
        }
    }

    var config: String

    init {
        val configJsonString = getConfigFromAssets("config.json")
        Log.i("TAG", ": config $configJsonString")
        config = JSONObject(configJsonString).get("config").toString()
        Log.i("TAG", "config JSON: $config")
    }

    private fun getConfigFromAssets(configFileName: String): String? {
        return try {
            val json = applicationContext.assets.open(configFileName).bufferedReader().use {
                it.readText()
            }
            json
        } catch (e: FileNotFoundException) {
            Log.e("TAG", "getConfigFromAssets: error while retriving config", e)
            null
        } catch (e: Exception) {
            Log.e("TAG", "getConfigFromAssets: error while retriving config", e)
            null
        }
    }

    fun saveWalletAsFile(walletJson: String) {
        createFile("wallet.json", walletJson)
    }

    fun getWalletModel(): WalletModel? {
        if (isWalletExist()) {
            var wallet = Gson().fromJson(readWalletFromFileJSON(), WalletModel::class.java)
            wallet.walletJson = readWalletFromFileJSON()
            return wallet
        } else
            return null
    }

    fun readWalletFromFileJSON(): String {
        return readFile("wallet.json")
    }

    fun isWalletExist(): Boolean {
        return !(readWalletFromFileJSON().isBlank() || readWalletFromFileJSON().isEmpty())
    }

    /* creates a file in the app's internal storage. */
    private fun createFile(fileName: String, content: String) {
        applicationContext.openFileOutput(fileName, Context.MODE_PRIVATE).use {
            it.write(content.toByteArray())
        }
    }

    private fun readFile(fileName: String): String {
        return try {
            applicationContext.openFileInput(fileName).bufferedReader().use {
                it.readText()
            }
        } catch (e: FileNotFoundException) {
            return ""
        }
    }


    fun getFileName(uri: Uri): String {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor: Cursor? =
                applicationContext.contentResolver.query(uri, null, null, null, null)
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result =
                        cursor.getStringOrNull(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                }
            } finally {
                cursor?.close()
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result!!.lastIndexOf('/')
            if (cut != -1) {
                result = result.substring(cut + 1)
            }
        }
        return result
    }

    fun getFileAbsolutePathFromUri(uri: Uri): String? {
        var path: String? = null
        val cursor: Cursor? =
            applicationContext.contentResolver.query(uri, null, null, null, null)
        if (cursor != null) {
            cursor.moveToFirst()
            val idx: Int = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
            path = cursor.getString(idx)
            cursor.close()
        }
        return path
    }

    fun getUriForFile(file: File): Uri {
        return Uri.fromFile(file)
    }

    fun getRealPathFromURI(uri: Uri): String? {
        when {
            // DocumentProvider
            DocumentsContract.isDocumentUri(applicationContext, uri) -> {
                when {
                    // ExternalStorageProvider
                    isExternalStorageDocument(uri) -> {
                        val docId = DocumentsContract.getDocumentId(uri)
                        val split = docId.split(":").toTypedArray()
                        val type = split[0]
                        // This is for checking Main Memory
                        return if ("primary".equals(type, ignoreCase = true)) {
                            if (split.size > 1) {
                                Environment.getExternalStorageDirectory()
                                    .toString() + "/" + split[1]
                            } else {
                                Environment.getExternalStorageDirectory().toString() + "/"
                            }
                            // This is for checking SD Card
                        } else {
                            "storage" + "/" + docId.replace(":", "/")
                        }
                    }
                    isDownloadsDocument(uri) -> {
                        val fileName = getFilePath(uri)
                        if (fileName != null) {
                            return Environment.getExternalStorageDirectory()
                                .toString() + "/Download/" + fileName
                        }
                        var id = DocumentsContract.getDocumentId(uri)
                        if (id.startsWith("raw:")) {
                            id = id.replaceFirst("raw:".toRegex(), "")
                            val file = File(id)
                            if (file.exists()) return id
                        }
                        val contentUri = ContentUris.withAppendedId(
                            Uri.parse("content://downloads/public_downloads"),
                            java.lang.Long.valueOf(id)
                        )
                        return getDataColumn(contentUri, null, null)
                    }
                    isMediaDocument(uri) -> {
                        val docId = DocumentsContract.getDocumentId(uri)
                        val split = docId.split(":").toTypedArray()
                        val type = split[0]
                        var contentUri: Uri? = null
                        when (type) {
                            "image" -> {
                                contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                            }
                            "video" -> {
                                contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                            }
                            "audio" -> {
                                contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                            }
                        }
                        val selection = "_id=?"
                        val selectionArgs = arrayOf(split[1])
                        return getDataColumn(
                            contentUri,
                            selection,
                            selectionArgs
                        )
                    }
                }
            }
            "content".equals(uri.scheme, ignoreCase = true) -> {
                // Return the remote address
                return if (isGooglePhotosUri(uri)) uri.lastPathSegment else getDataColumn(
                    uri,
                    null,
                    null
                )
            }
            "file".equals(uri.scheme, ignoreCase = true) -> {
                return uri.path
            }
        }
        return null
    }

    fun getDataColumn(
        uri: Uri?, selection: String?,
        selectionArgs: Array<String>?
    ): String? {
        var cursor: Cursor? = null
        val column = "_data"
        val projection = arrayOf(
            column
        )
        try {
            if (uri == null) return null
            cursor = applicationContext.contentResolver.query(
                uri, projection, selection, selectionArgs,
                null
            )
            if (cursor != null && cursor.moveToFirst()) {
                val index = cursor.getColumnIndexOrThrow(column)
                return cursor.getString(index)
            }
        } finally {
            cursor?.close()
        }
        return null
    }


    fun getFilePath(uri: Uri?): String? {
        var cursor: Cursor? = null
        val projection = arrayOf(
            MediaStore.MediaColumns.DISPLAY_NAME
        )
        try {
            if (uri == null) return null
            cursor = applicationContext.contentResolver.query(
                uri, projection, null, null,
                null
            )
            if (cursor != null && cursor.moveToFirst()) {
                val index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
                return cursor.getString(index)
            }
        } finally {
            cursor?.close()
        }
        return null
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    fun isExternalStorageDocument(uri: Uri): Boolean {
        return "com.android.externalstorage.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    fun isDownloadsDocument(uri: Uri): Boolean {
        return "com.android.providers.downloads.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    fun isGooglePhotosUri(uri: Uri): Boolean {
        return "com.google.android.apps.photos.content" == uri.authority
    }
}
