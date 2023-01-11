package org.zus.bolt.helloworld.models.blobber

data class BlobberNodeModelItem(
    val allocated: Long,
    val capacity: Long,
    val id: String,
    val last_health_check: Int,
    val stake_pool_settings: StakePoolSettings,
    val terms: TermsX,
    val total_stake: Long,
    val url: String
)
