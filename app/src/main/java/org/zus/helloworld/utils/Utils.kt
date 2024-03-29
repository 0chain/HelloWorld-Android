package org.zus.helloworld.utils

import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.webkit.MimeTypeMap
import android.webkit.URLUtil
import android.widget.EditText
import androidx.core.database.getStringOrNull
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.json.JSONException
import org.json.JSONObject
import org.zus.helloworld.models.bolt.TransactionModel
import org.zus.helloworld.models.bolt.WalletModel
import org.zus.helloworld.ui.TAG_CREATE_WALLET
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class Utils(private var applicationContext: Context) {
    companion object {
        const val ATLUS_BASE_URL = "https://atlus.cloud"

        fun String.getAtlusURL(): String {
            val hash = this
            return "$ATLUS_BASE_URL/transaction-details/$hash?network=demo.zus.network"
        }

        fun Int.getConvertedDateTime(): String {
            val s = this
            return try {
                val sdf = SimpleDateFormat("dd/MM 'at' HH:mm:ss", Locale.ENGLISH)
                val netDate = Date(s.toLong() * 1000L)
                sdf.format(netDate)
            } catch (e: Exception) {
                e.toString()
            }
        }

        fun Long.getConvertedDateTime(): String {
            val s = this
            return try {
                val sdf = SimpleDateFormat("dd/MM 'at' HH:mm:ss", Locale.ENGLISH)
                val netDate = Date(s * 1000L)
                sdf.format(netDate)
            } catch (e: Exception) {
                e.toString()
            }
        }

        fun Long.getConvertedTime(): String {
            val s = this
            return try {
                val sdf = SimpleDateFormat("dd/MM, HH:mm", Locale.ENGLISH)
                val netDate = Date(s * 1000L)
                sdf.format(netDate)
            } catch (e: Exception) {
                e.toString()
            }
        }

        fun Int.getConvertedSize(): String {
            val size = this
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

        fun Long.getConvertedSize(): String {
            val size = this
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

        fun Long.getSizeInB(type: StorageSizes): Long {
            val size = this
            return when (type) {
                StorageSizes.B -> {
                    size
                }

                StorageSizes.KB -> {
                    size * 1024
                }

                StorageSizes.MB -> {
                    size * 1024 * 1024
                }

                StorageSizes.GB -> {
                    size * 1024 * 1024 * 1024
                }
            }
        }

        fun Long.getTimeInNanoSeconds(type: ExpirationTime): Long {
            val time = this
            return when (type) {
                ExpirationTime.NanoSecond -> {
                    time
                }

                ExpirationTime.MicroSecond -> {
                    time * 1000
                }

                ExpirationTime.MilliSecond -> {
                    time * 1000 * 1000
                }

                ExpirationTime.Second -> {
                    time * 1000 * 1000 * 1000
                }

                ExpirationTime.Minute -> {
                    time * 1000 * 1000 * 1000 * 60
                }

                ExpirationTime.Hour -> {
                    time * 1000 * 1000 * 1000 * 60 * 60
                }
            }
        }

        fun String.getShortFormattedString(): String {
            val string = this
            return if (string.length > 10) {
                "${string.substring(0, 9)}...${string.substring(string.length - 4, string.length)}"
            } else {
                string
            }
        }

        fun String.isValidUrl(): Boolean = URLUtil.isValidUrl(this)

        fun String.isValidJson(): Boolean {
            val string = this
            return try {
                JSONObject(string)
                true
            } catch (e: JSONException) {
                Log.e(TAG_CREATE_WALLET, "isValidJson: Exception", e)
                Log.e(TAG_CREATE_WALLET, "json string $this")
                false
            }
        }

        fun List<TransactionModel>.mergeListsWithoutDuplicates(
            list2: List<TransactionModel>
        ): List<TransactionModel> {
            val list1 = this
            val listString1 = list1.map { it.hash }
            val listString2 = list2.map { it.hash }
            val list = mutableListOf<TransactionModel>()
            val listString = mutableListOf<String>()
            listString.addAll(listString1)
            listString.addAll(listString2)
            val uniqueListString = listString.distinct()
            for (item in uniqueListString) {
                val transactionModel = list1.find { it.hash == item }
                if (transactionModel != null) {
                    list.add(transactionModel)
                } else {
                    val transactionModel2 = list2.find { it.hash == item }
                    if (transactionModel2 != null) {
                        list.add(transactionModel2)
                    }
                }
            }
            //Todo: Test this more as for now it seems to work.
            list.sortedByDescending {
                it.creation_date
            }
            return list
        }

        fun String.prettyJsonFormat(): String {
            val string = this
            return try {
                GsonBuilder().setPrettyPrinting().create().toJson(
                    Gson().fromJson(string, Any::class.java)
                )
            } catch (e: JSONException) {
                Log.e(TAG_CREATE_WALLET, "isValidJson: Exception", e)
                Log.e(TAG_CREATE_WALLET, "json string $this")
                string
            }
        }

        fun convertEpochTimeToMillis(dates: String): Long {
            val epochDate = dates.toInt()
            return epochDate.toLong()
        }
    }

    var config: String

    init {
        val configJsonString = getConfigFromAssets("config.json")
        Log.i("TAG", ": config $configJsonString")
        config = JSONObject(configJsonString).get("config").toString()
        Log.i("TAG", "config JSON: $config")
    }

    fun getConfigFromAssets(configFileName: String): String? {
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
        val files = applicationContext.filesDir
        val file = File(files, fileName)
        file.writeText(content)
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

    private fun getDataColumn(
        uri: Uri?, selection: String?,
        selectionArgs: Array<String>?,
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


    private fun getFilePath(uri: Uri?): String? {
        var cursor: Cursor? = null
        val projection = arrayOf(
            MediaStore.MediaColumns.DATA
        )
        try {
            if (uri == null) return null
            cursor = applicationContext.contentResolver.query(
                uri, projection, null, null,
                null
            )
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getString(0)
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
    private fun isExternalStorageDocument(uri: Uri): Boolean {
        return "com.android.externalstorage.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    private fun isDownloadsDocument(uri: Uri): Boolean {
        return "com.android.providers.downloads.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    private fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    private fun isGooglePhotosUri(uri: Uri): Boolean {
        return "com.google.android.apps.photos.content" == uri.authority
    }
    private val okFileExtensions = arrayOf(
        "jpg",
        "png",
        "gif",
        "jpeg"
    )

    fun getRootDir(allocID: String): String? {
        val CAMERA_FOLDER = File(
            "" + Environment.getExternalStorageDirectory().toString() + "/DCIM/Camera/" + allocID
        )
        return CAMERA_FOLDER.path
    }

    @Throws(IOException::class)
    fun rootDirCreate(allocationID: String) {
        val rootDir = getRootDir(allocationID) ?: throw IOException("error with root dir")
        val root = File(rootDir)
        if (root != null && !root.exists()) {
            root.mkdirs()
        }
    }

    fun hideKeyboard(input: EditText, context: Context) {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(input.windowToken, 0)
    }

    fun isImage(file: File): Boolean {
        for (extension in okFileExtensions) {
            if (file.name.lowercase(Locale.getDefault()).endsWith(extension)) {
                return true
            }
        }
        return false
    }
    fun getMimeType(file: File): String? {
        val fileName = file.name
        val fileExtension = fileName.substringAfterLast(".", "")
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension)
    }

}
