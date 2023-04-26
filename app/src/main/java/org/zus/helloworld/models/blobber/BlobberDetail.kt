package org.zus.helloworld.models.blobber

data class BlobberDetail(
    val blobber_id: String,
    val challenge_reward: Int,
    val final_reward: Int,
    val min_lock_demand: Int,
    val penalty: Int,
    val read_reward: Int,
    val returned: Int,
    val size: Long,
    val spent: Int,
    val terms: Terms
)
