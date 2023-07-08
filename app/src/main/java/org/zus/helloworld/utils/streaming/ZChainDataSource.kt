package org.zus.helloworld.utils.streaming

import android.net.Uri
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.upstream.BaseDataSource
import com.google.android.exoplayer2.upstream.DataSpec
import com.google.android.exoplayer2.upstream.HttpDataSource
import com.google.android.exoplayer2.upstream.HttpDataSource.HttpDataSourceException
import com.google.android.exoplayer2.upstream.HttpDataSource.RequestProperties
import com.google.android.exoplayer2.upstream.TransferListener
import com.google.android.exoplayer2.util.Assertions
import com.google.android.exoplayer2.util.Log
import com.google.android.exoplayer2.util.Util
import zbox.Allocation
import java.io.EOFException
import java.io.IOException
import java.util.*
import java.util.concurrent.CountDownLatch
import kotlin.math.max

class ZChainDataSource private constructor(
    allocation: Allocation,
    file: ZChainFile
) : BaseDataSource( /* isNetwork= */false), HttpDataSource {
    private val requestProperties: RequestProperties
    var writePosition = 0
    private var dataSpec: DataSpec? = null
    private val skipBuffer: ByteArray? = null
    private val opened = false
    private var responseCode = 0
    private val bytesToSkip: Long = 0
    private var bytesToRead: Long = 0
    private var bytesSkipped: Long = 0
    private var bytesRead: Long = 0
    private val allocation: Allocation
    private var dataRequestStarted = false
    private var currentBlockRead: Long = 0
    private val numBlocksToDownload = 5
    private val doneSignal = CountDownLatch(1)
    private var totalFileBlocks: Long = 0
    private val file: ZChainFile
    private var fileRemotePath = ""
    private var streamingData: ByteArray? = ByteArray(INITIAL_STREAMING_DATA_SIZE)
    private var readPosition = 0
    private var bytesRemaining = 0
    private val dataShards = 2
    private val DEFAULT_CHUNK_SIZE = 65536L

    init {
        requestProperties = RequestProperties()
        this.allocation = allocation
        this.file = file
        fileRemotePath = (file as ZChainLocalFile).fileRemotePath

    }

    private fun startDataRequest(dataSpec: DataSpec) {
        dataRequestStarted = true
        if (file.numBlocks!! > 0) {
            if (totalFileBlocks == 0L) {
                val effectivePerShardSize: Long =
                    (file.fileTotalBytes!! + dataShards - 1) / dataShards
                val effectiveBlockSize =
                    if (file.chunkSize == 0L) DEFAULT_CHUNK_SIZE else file.chunkSize
                totalFileBlocks =
                    (effectivePerShardSize + effectiveBlockSize - 1) / effectiveBlockSize
            }
            if (dataSpec.position != 0L) {
                proceedBlockRead(dataSpec.position)
            } else {
                val INITIAL_BLOCK = 1
                proceedBlockRead(INITIAL_BLOCK.toLong())
            }
        }
    }

    fun proceedBlockRead(blockNumber: Long) {
        currentBlockRead = blockNumber
        val blockSize = if (file.chunkSize == 0L) DEFAULT_CHUNK_SIZE else file.chunkSize
        val startIndex = ((blockNumber - 1) * blockSize).toInt() * dataShards
        val localPath: String =
            file.tempPath + "/" + hashCode() + fileRemotePath + ".ch-" + currentBlockRead
        Log.d("debug", "Proceed read $localPath")
        val callback = ZBoxCallback(localPath, object : DownloadCallback() {
            override fun completed(data: ByteArray?) {
                if (data == null || data.size == 0) {
                    responseCode = 500
                    Log.d("debug", "No data in response")
                    return
                }
                responseCode = 200
                Log.d(
                    "debug",
                    "currentBlockRead $currentBlockRead num $totalFileBlocks"
                )
                if (currentBlockRead < totalFileBlocks) {
                    proceedBlockRead(currentBlockRead + 1)
                }
            }

            override fun inProgress(data: ByteArray?) {
                if (data != null && data.size > 0) {
                    responseCode = 200
                    val endIndex = startIndex + data.size
                    if (endIndex > streamingData!!.size) {
                        val expandedData = ByteArray(endIndex)
                        System.arraycopy(streamingData, 0, expandedData, 0, writePosition)
                        streamingData = expandedData
                    }
                    System.arraycopy(data, 0, streamingData, startIndex, data.size)
                    writePosition = endIndex
                    if (writePosition >= readPosition) doneSignal.countDown()
                }
            }

            override fun error(e: Exception?) {
                responseCode = 500
                e!!.printStackTrace()
                doneSignal.countDown()
            }
        })
        val startBlock = currentBlockRead
        val endBlock = Math.min(totalFileBlocks, startBlock + numBlocksToDownload)
        currentBlockRead = endBlock
        transferStarted(dataSpec!!)
        if (endBlock >= startBlock) {
            try {
                Log.i(
                    "BLOCK_DOWNLOAD",
                    "Downloading blocks from $startBlock to $endBlock"
                )
                val BLOCKS_PE_TRANSACTION = 10
                    allocation.downloadFileByBlock(
                        fileRemotePath,
                        localPath,
                        startBlock,
                        endBlock,
                        BLOCKS_PE_TRANSACTION.toLong(),
                        callback,
                        true
                    )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            Log.d(
                "debug",
                "finished download total: $totalFileBlocks downloaded $currentBlockRead"
            )
        }
    }

    override fun getUri(): Uri? {
        return Uri.parse("@0chain://$fileRemotePath")
    }

    override fun getResponseCode(): Int {
        return 200
    }

    override fun getResponseHeaders(): Map<String, List<String>> {
        val fakeHeaders: MutableMap<String, List<String>> = HashMap()
        fakeHeaders["Content-Type"] = listOf(file.mimeType)
        fakeHeaders["Content-Length"] =
            listOf(java.lang.String.valueOf(file.fileTotalBytes))
        fakeHeaders["X-Android-Response-Source"] = listOf("NETWORK 200")
        fakeHeaders["X-Android-Selected-Protocol"] = listOf("http/1.1")
        return fakeHeaders
    }

    override fun setRequestProperty(name: String, value: String) {
        Assertions.checkNotNull(name)
        Assertions.checkNotNull(value)
        requestProperties[name] = value
    }

    override fun clearRequestProperty(name: String) {
        Assertions.checkNotNull(name)
        requestProperties.remove(name)
    }

    override fun clearAllRequestProperties() {
        requestProperties.clear()
    }

    /**
     * Opens the source to read the specified data.
     */
    @Throws(HttpDataSourceException::class)
    override fun open(dataSpec: DataSpec): Long {
        this.dataSpec = dataSpec
        bytesRead = 0
        bytesSkipped = 0
        transferInitializing(dataSpec)
        bytesToRead = file.fileTotalBytes!!
        if (streamingData == null || streamingData!!.size < bytesToRead) {
            val initialSize = Math.max(bytesToRead.toInt(), INITIAL_STREAMING_DATA_SIZE)
            streamingData = ByteArray(initialSize)
        }
        transferStarted(dataSpec)
        if (!dataRequestStarted) {
            Log.d("debug", " start wait $bytesToRead")
            startDataRequest(dataSpec)
            try {
                doneSignal.await()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
            Log.d("debug", " continue $bytesToRead")
        }
        readPosition = dataSpec.position.toInt()
        bytesRemaining =
            (if (dataSpec.length == C.LENGTH_UNSET.toLong()) writePosition - dataSpec.position else readPosition).toInt()
        Log.i(
            "debug",
            "open - bytesRemaining " + bytesRemaining + " dataSpec.length " + dataSpec.length + " dataSpec.position " + dataSpec.position
        )
        return if (bytesRemaining <= 0) C.LENGTH_UNSET.toLong() else bytesRemaining.toLong()
    }

    @Throws(HttpDataSourceException::class)
    override fun read(buffer: ByteArray, offset: Int, readLength: Int): Int {
        return try {
            readInternal(buffer, offset, readLength)
        } catch (e: IOException) {
            throw HttpDataSourceException(
                e, Util.castNonNull(dataSpec), HttpDataSourceException.TYPE_READ
            )
        }
    }

    @Throws(HttpDataSourceException::class)
    override fun close() {
        Log.i("debug", " ===> closing <===")
    }

    /**
     * Reads up to `length` bytes of data and stores them into `buffer`, starting at
     * index `offset`.
     *
     *
     * This method blocks until at least one byte of data can be read, the end of the opened range is
     * detected, or an exception is thrown.
     *
     * @param buffer     The buffer into which the read data should be stored.
     * @param offset     The start offset into `buffer` at which data should be written.
     * @param readLength The maximum number of bytes to read.
     * @return The number of bytes read, or [C.RESULT_END_OF_INPUT] if the end of the opened
     * range is reached.
     * @throws IOException If an error occurs reading from the source.
     */
    @Throws(IOException::class)
    private fun readInternal(buffer: ByteArray, offset: Int, readLength: Int): Int {
        var readLength = readLength
        if (readLength == 0) {
            return 0
        } else if (writePosition == 0 || writePosition < readLength) {
            throw EOFException()
        }
        if (writePosition < readPosition) {
            return 0
        }
        readLength = Math.min(readLength, writePosition)
        Log.i(
            "debug",
            "==> readPosition $readPosition offset $offset readLength $readLength"
        )
        System.arraycopy(streamingData, readPosition, buffer, offset, readLength)
        readPosition += readLength
        bytesRemaining -= readLength
        return readLength
    }

    fun openFromCustomPoint(dataSpec: DataSpec) {
        this.dataSpec = dataSpec
        bytesRead = 0
        bytesSkipped = 0
        transferInitializing(dataSpec)
        bytesToRead = file.fileTotalBytes!!
        if (streamingData == null || streamingData!!.size < bytesToRead) {
            val initialSize = max(bytesToRead.toInt(), INITIAL_STREAMING_DATA_SIZE)
            streamingData = ByteArray(initialSize)
        }
        transferStarted(dataSpec)
        if (!dataRequestStarted) {
            Log.d("CUSTOM_POINT", " start wait $bytesToRead")
            startDataRequest(dataSpec)
            Log.d("CUSTOM_POINT", " continue $bytesToRead")
        }
        readPosition = dataSpec.position.toInt()
        bytesRemaining =
            (if (dataSpec.length == C.LENGTH_UNSET.toLong()) writePosition - dataSpec.position else readPosition).toInt()
        Log.i(
            "CUSTOM_POINT",
            "open - bytesRemaining " + bytesRemaining + " dataSpec.length " + dataSpec.length + " dataSpec.position " + dataSpec.position
        )
    }

    /**
     * [ZChainDataSource.Factory] for [ZChainDataSource] instances.
     */
    class Factory(allocation: Allocation, file: ZChainFile) :
        HttpDataSource.Factory {
        private val defaultRequestProperties: RequestProperties
        private var transferListener: TransferListener? = null
        private val allocation: Allocation
        private val file: ZChainFile

        init {
            defaultRequestProperties = RequestProperties()
            this.allocation = allocation
            this.file = file
        }

        /**
         * Sets the [TransferListener] that will be used.
         *
         *
         * The default is `null`.
         *
         * @param transferListener The listener that will be used.
         * @return This factory.
         */
        fun setTransferListener(transferListener: TransferListener?): Factory {
            this.transferListener = transferListener
            return this
        }

        override fun createDataSource(): ZChainDataSource {
            val dataSource = ZChainDataSource(
                allocation,
                file
            )
            if (transferListener != null) {
                dataSource.addTransferListener(transferListener!!)
            }
            return dataSource
        }

        override fun setDefaultRequestProperties(defaultRequestProperties: Map<String, String>): HttpDataSource.Factory {
            throw UnsupportedOperationException("setDefaultRequestProperties is not supported.")
        }
    }

    companion object {
        private const val TAG = "ZChainDataSource"
        private const val INITIAL_STREAMING_DATA_SIZE = 1024 * 1024 // 1MB
    }
}
