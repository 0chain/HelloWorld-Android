package org.zus.helloworld.utils.streaming

import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.drm.DrmSessionManagerProvider
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.MediaSourceFactory
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DataSpec
import com.google.android.exoplayer2.upstream.HttpDataSource.HttpDataSourceException
import com.google.android.exoplayer2.upstream.LoadErrorHandlingPolicy

class ZChainMediaSourceFactory(dataSourceFactory: ZChainDataSource.Factory) :
    MediaSourceFactory {
    private val dataSourceFactory: ZChainDataSource.Factory
    private val progressiveMediaSourceFactory: ProgressiveMediaSource.Factory
    private var zChainDataSource: ZChainDataSource? = null

    init {
        this.dataSourceFactory = dataSourceFactory
        progressiveMediaSourceFactory = ProgressiveMediaSource.Factory(dataSourceFactory)
    }

    override fun createMediaSource(mediaItem: MediaItem): MediaSource {
        zChainDataSource = dataSourceFactory.createDataSource() as ZChainDataSource
        return progressiveMediaSourceFactory.createMediaSource(mediaItem)
    }

    override fun setDrmSessionManagerProvider(drmSessionManagerProvider: DrmSessionManagerProvider): MediaSource.Factory {
        throw UnsupportedOperationException("setDrmSessionManagerProvider is not supported.")
    }

    override fun setLoadErrorHandlingPolicy(loadErrorHandlingPolicy: LoadErrorHandlingPolicy): MediaSource.Factory {
        throw UnsupportedOperationException("setLoadErrorHandlingPolicy is not supported.")
    }

    override fun getSupportedTypes(): IntArray {
        return progressiveMediaSourceFactory.supportedTypes
    }

    @Throws(HttpDataSourceException::class)
    fun setDataSpec(dataSpec: DataSpec?) {
        if (dataSpec != null) {
            zChainDataSource?.openFromCustomPoint(dataSpec)
        }
    }
}