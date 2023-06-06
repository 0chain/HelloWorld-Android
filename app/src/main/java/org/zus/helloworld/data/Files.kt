package org.zus.helloworld.data

import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import org.json.JSONObject
import org.zus.helloworld.utils.Utils
import java.io.File
import java.util.*

class Files : Parcelable, Cloneable {

    private var lookupHash: String? = null

    var hash: String? = null

    var name: String? = null

    var path: String? = null

    var type: String? = null

    var size: Long = 0

    var mimeType: String? = null

    var numBlocks = 0

    var fileMode = 0

    var fileType = 0

    lateinit var remotePath: String

    var isEncrypted = false

    var devicePath: String? = null

    var createdAt: Long = 0

    var updatedAt: Long = 0

    var thumbnailPath: String? = null

    var totalSize: Long = 0

    var imagePath: File? = null

    var imageUri: File? = null

    var imageUris: Uri? = null

    var blobberURL: String? = null

    var uploads = 0

    var downloads = 0

    var challenges = 0

    var blockChainAware: String? = null

    var actualFileSize: Long = 0

    var actualBlocks: Long = 0

    var uploadDir: File? = null

    var thumbDir: File? = null

    var parent: Files? = null

    var uploadedBytes: Long = 0

    private var uploadStatus //1 started 2 // Progress 3 // error 4 // completed
            = 0

    private var isUploading = false


    private var androidPath: String? = null

    private var readOnly = false

    protected constructor(`in`: Parcel) {
        name = `in`.readString()
        path = `in`.readString()
        type = `in`.readString()
        size = `in`.readLong()
        hash = `in`.readString()
        mimeType = `in`.readString()
        numBlocks = `in`.readInt()
        lookupHash = `in`.readString()!!
        fileMode = `in`.readInt()
        fileType = `in`.readInt()
        remotePath = `in`.readString()!!
        isUploading = `in`.readByte().toInt() != 0
        isEncrypted = `in`.readByte().toInt() != 0
        devicePath = `in`.readString()
        createdAt = `in`.readLong()
        uploadStatus = `in`.readInt()
        blobberURL = `in`.readString()
        uploads = `in`.readInt()
        downloads = `in`.readInt()
        challenges = `in`.readInt()
        blockChainAware = `in`.readString()
        actualFileSize = `in`.readLong()
        actualBlocks = `in`.readLong()
        androidPath = `in`.readString()
        thumbnailPath = `in`.readString()
        readOnly = `in`.readByte().toInt() != 0
    }

    constructor(
        uriType: Uri?,
        fileType: File?,
        filename: String?,
        path: String?,
        fileTypeFile: String?,
        size: Long,
        hash: String?,
        mimeType: String?,
        numBlocks: Int,
        lookupHash: String,
        fileMode: Int,
        remotePath: String,
        uploadDirs: File?,
        thumbDir: File?,
        isUploading: Boolean,
        encryptedStatus: Boolean,
        createdAt: Long,
        uploadStatus: Int,
        updatedAt: Long
    ) {
        imageUris = uriType
        imageUri = fileType
        name = filename
        this.path = path
        type = fileTypeFile
        this.size = size
        this.hash = hash
        this.mimeType = mimeType
        this.numBlocks = numBlocks
        this.lookupHash = lookupHash
        this.fileMode = fileMode
        this.remotePath = remotePath
        uploadDir = uploadDirs
        this.thumbDir = thumbDir
        this.isUploading = isUploading
        isEncrypted = encryptedStatus
        this.createdAt = createdAt
        this.uploadStatus = uploadStatus
        this.updatedAt = updatedAt
    }

    constructor(name: String?, mimeType: String?, imagePath: File?) {
        this.name = name
        this.mimeType = mimeType
        this.imagePath = imagePath
    }

    constructor(
        size: Long,
        blobberURL: String?,
        uploads: Int,
        downloads: Int,
        challenges: Int,
        blockChainAware: String?
    ) {
        this.size = size
        this.blobberURL = blobberURL
        this.uploads = uploads
        this.downloads = downloads
        this.challenges = challenges
        this.blockChainAware = blockChainAware
    }

    constructor(name: String?, progress: Int) {
        this.name = name
    }

    /**
     * Auto Upload Feature New
     *
     * @param name
     * @param path
     * @param type
     * @param size
     * @param hash
     * @param mimeType
     * @param numBlocks
     * @param lookupHash
     * @param fileMode
     * @param remotePath
     * @param uploadDir
     * @param thumbDir
     * @param isUploading
     * @param isEncrypted
     * @param devicePath
     */
    constructor(
        imageUri: File?,
        name: String?,
        path: String?,
        type: String?,
        size: Long,
        hash: String?,
        mimeType: String?,
        numBlocks: Int,
        lookupHash: String,
        fileMode: Int,
        remotePath: String,
        uploadDir: File?,
        thumbDir: File?,
        isUploading: Boolean,
        isEncrypted: Boolean,
        devicePath: String?
    ) {
        //this.imageUri = imageUri;
        this.imageUri = imageUri
        this.name = name
        this.path = path
        this.type = type
        this.size = size
        this.hash = hash
        this.mimeType = mimeType
        this.numBlocks = numBlocks
        this.lookupHash = lookupHash
        this.fileMode = fileMode
        this.remotePath = remotePath
        this.uploadDir = uploadDir
        this.thumbDir = thumbDir
        this.isUploading = isUploading
        this.isEncrypted = isEncrypted
        this.devicePath = devicePath
    }

    /**
     * Auto Upload Feature
     *
     * @param name
     * @param path
     * @param type
     * @param size
     * @param hash
     * @param mimeType
     * @param numBlocks
     * @param lookupHash
     * @param fileMode
     * @param remotePath
     * @param uploadDir
     * @param thumbDir
     * @param isUploading
     * @param isEncrypted
     * @param devicePath
     */
    constructor(
        name: String?,
        path: String?,
        type: String?,
        size: Long,
        hash: String?,
        mimeType: String?,
        numBlocks: Int,
        lookupHash: String,
        fileMode: Int,
        remotePath: String,
        uploadDir: File?,
        thumbDir: File?,
        isUploading: Boolean,
        isEncrypted: Boolean,
        devicePath: String?
    ) {
        this.name = name
        this.path = path
        this.type = type
        this.size = size
        this.hash = hash
        this.mimeType = mimeType
        this.numBlocks = numBlocks
        this.lookupHash = lookupHash
        this.fileMode = fileMode
        this.remotePath = remotePath
        this.uploadDir = uploadDir
        this.thumbDir = thumbDir
        this.isUploading = isUploading
        this.isEncrypted = isEncrypted
        this.devicePath = devicePath
    }

    /**
     * Normal File Upload ArrayList New
     *
     * @param name
     * @param path
     * @param type
     * @param size
     * @param hash
     * @param mimeType
     * @param numBlocks
     * @param lookupHash
     * @param fileMode
     * @param remotePath
     * @param uploadDir
     * @param thumbDir
     * @param isUploading
     * @param isEncrypted
     */
    constructor(
        imageUri: File?,
        name: String?,
        path: String?,
        type: String?,
        size: Long,
        hash: String?,
        mimeType: String?,
        numBlocks: Int,
        lookupHash: String,
        fileMode: Int,
        remotePath: String,
        uploadDir: File?,
        thumbDir: File?,
        isUploading: Boolean,
        isEncrypted: Boolean,
        createdAt: Long,
        uploadStatus: Int
    ) {
        this.imageUri = imageUri
        this.name = name
        this.path = path
        this.type = type
        this.size = size
        this.hash = hash
        this.mimeType = mimeType
        this.numBlocks = numBlocks
        this.lookupHash = lookupHash
        this.fileMode = fileMode
        this.remotePath = remotePath
        this.uploadDir = uploadDir
        this.thumbDir = thumbDir
        this.isUploading = isUploading
        this.isEncrypted = isEncrypted
        this.createdAt = createdAt
        this.uploadStatus = uploadStatus
    }

    constructor() {}

    fun isReadOnly(): Boolean {
        return readOnly
    }

    fun setReadOnly(readOnly: Boolean) {
        this.readOnly = readOnly
    }

    val formattedFileSize: String
        get() = formatSize(size)

    fun isUploadStatus(): Int {
        return uploadStatus
    }

    fun getUploadStatus(): Int {
        return uploadStatus
    }

    fun setUploadStatus(uploadStatus: Int) {
        this.uploadStatus = uploadStatus
    }

    fun getAndroidPath(): String? {
        return androidPath
    }

    fun setAndroidPath(androidPath: String?) {
        this.androidPath = androidPath
    }

    fun getLookupHash(): String? {
        return lookupHash
    }

    fun setLookupHash(lookupHash: String) {
        this.lookupHash = lookupHash
    }

    fun isUploading(): Boolean {
        return isUploading
    }

    fun setUploading(uploading: Boolean) {
        isUploading = uploading
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(parcel: Parcel, i: Int) {
        parcel.writeString(name)
        parcel.writeString(path)
        parcel.writeString(type)
        parcel.writeLong(size)
        parcel.writeString(hash)
        parcel.writeString(mimeType)
        parcel.writeInt(numBlocks)
        parcel.writeString(lookupHash)
        parcel.writeInt(fileMode)
        parcel.writeInt(fileType)
        parcel.writeString(remotePath)
        parcel.writeByte((if (isUploading) 1 else 0).toByte())
        parcel.writeByte((if (isEncrypted) 1 else 0).toByte())
        parcel.writeString(devicePath)
        parcel.writeLong(createdAt)
        parcel.writeInt(uploadStatus)
        //parcel.writeParcelable(imageUri, i);
        parcel.writeString(blobberURL)
        parcel.writeInt(uploads)
        parcel.writeInt(downloads)
        parcel.writeInt(challenges)
        parcel.writeString(blockChainAware)
        parcel.writeLong(actualFileSize)
        parcel.writeLong(actualBlocks)
        parcel.writeString(androidPath)
        parcel.writeString(thumbnailPath)
        parcel.writeByte((if (readOnly) 1 else 0).toByte())
    }

    override fun toString(): String {
        return "ZboxFilesModel{" +
                "name='" + name + '\'' +
                '}'
    }

    override fun equals(obj: Any?): Boolean {
        if (obj === this) return true
        if (obj !is Files) return false
        val file = obj
        return if (file.getLookupHash() == null) getLookupHash() == null else file.getLookupHash() == getLookupHash()
    }

    override fun hashCode(): Int {
        return Objects.hash(getLookupHash())
    }

    public override fun clone(): Files {
        return try {
            super.clone() as Files
        } catch (e: CloneNotSupportedException) {
            this
        }
    }

    companion object {
        const val STATUS_STARTED = 1
        const val STATUS_PROGRESS_DOWNLOAD = 2
        const val STATUS_ERROR = 3
        const val STATUS_COMPLETED = 4
        const val STATUS_PROGRESS_UPLOAD = 5
        @JvmField
        val CREATOR: Parcelable.Creator<Files?> = object : Parcelable.Creator<Files?> {
            override fun createFromParcel(`in`: Parcel): Files? {
                return Files(`in`)
            }

            override fun newArray(size: Int): Array<Files?> {
                return arrayOfNulls(size)
            }
        }

        @Throws(Exception::class)
        fun fromJson(fileObj: JSONObject, allocationID: String): Files {
            val createdAt =
                if (fileObj.has("created_at")) Utils.convertEpochTimeToMillis(fileObj.getString("created_at")) else System.currentTimeMillis() / 1000
            val updatedAt =
                if (fileObj.has("updated_at")) Utils.convertEpochTimeToMillis(fileObj.getString("created_at")) else System.currentTimeMillis() / 1000
            // TODO refactor to match newest version of gosdk
            val fileType =
                if (fileObj.has("type")) fileObj.getString("type") else fileObj.getString("Type")
            val filePath =
                if (fileObj.has("path")) fileObj.getString("path") else fileObj.getString("Path")
            val hash =
                if (fileObj.has("hash")) fileObj.getString("hash") else if (fileObj.has("Hash")) fileObj.getString(
                    "Hash"
                ) else ""
            val lookupHash =
                if (fileObj.has("lookup_hash")) fileObj.getString("lookup_hash") else fileObj.getString(
                    "LookupHash"
                )
            val mimeType =
                if (fileObj.has("mimetype")) fileObj.getString("mimetype") else if (fileObj.has("MimeType")) fileObj.getString(
                    "MimeType"
                ) else ""
            val size =
                if (fileObj.has("Size")) fileObj.getLong("Size") else if (fileObj.has("size")) fileObj.getLong(
                    "size"
                ) else 0L
            val file = Files(
                null,
                null,
                if (fileObj.has("name")) fileObj.getString("name") else fileObj.getString("Name"),
                filePath,
                fileType,
                size,
                if (fileType == "f") hash else "",
                if (fileType == "f") mimeType else "",
                if (fileObj.has("num_blocks")) fileObj.getInt("num_blocks") else 0,
                lookupHash,
                0,
                filePath,
                File("uploadDir"),
                File("thumbDir"),
                true,
                fileObj.has("encryption_key") && fileObj.getString("encryption_key") != "" || fileObj.has(
                    "EncryptedKey"
                ) && fileObj.getString("EncryptedKey") != "",
                createdAt,
                0, updatedAt
            )
            if (fileObj.has("actual_num_blocks")) {
                file.actualBlocks = fileObj.getLong("actual_num_blocks")
            }
            if (fileObj.has("actual_size")) {
                file.actualFileSize = fileObj.getLong("actual_size")
            } else if (fileObj.has("ActualFileSize")) {
                file.actualFileSize = fileObj.getLong("ActualFileSize")
            }
            file.uploadStatus = STATUS_COMPLETED
            return file
        }

        fun newUploadingFile(
            name: String?,
            remotePath: String,
            actualPath: String?
        ): Files {
            val lookupHash = "hash-$remotePath"
            val file = Files(
                null,
                null,
                name,
                remotePath,
                "f",
                0L,
                "",
                "",
                0,
                lookupHash,
                0,
                remotePath,
                File("uploadDir"),
                File("thumbDir"),
                true,
                false,
                System.currentTimeMillis() / 1000,
                0, System.currentTimeMillis() / 1000
            )
            file.uploadStatus = 1
            file.isUploading = true
            file.androidPath = actualPath
            return file
        }

        fun newReadOnly(
            name: String?,
            remotePath: String,
            path: String?,
            actualPath: String?,
            allocationId: String,
            fileType: String?
        ): Files {
            val lookupHash = "hash-$remotePath"
            val file = Files(
                null,
                null,
                name,
                path,
                fileType,
                0L,
                "",
                "",
                0,
                lookupHash,
                0,
                remotePath,
                File("uploadDir"),
                File("thumbDir"),
                true,
                false,
                System.currentTimeMillis(),
                0, System.currentTimeMillis()
            )
            file.uploadStatus = STATUS_COMPLETED
            file.isUploading = false
            file.androidPath = actualPath
            file.readOnly = true
            return file
        }

        // TODO move to utils
        fun formatSize(v: Long): String {
            if (v < 1024) return "$v B"
            val z = (63 - java.lang.Long.numberOfLeadingZeros(v)) / 10
            return String.format("%.1f %sB", v.toDouble() / (1L shl z * 10), " KMGTPE"[z])
        }
    }


}