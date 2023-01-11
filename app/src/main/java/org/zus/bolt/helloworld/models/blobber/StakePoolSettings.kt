package org.zus.bolt.helloworld.models.blobber

data class StakePoolSettings(
    val delegate_wallet: String,
    val max_stake: Long,
    val min_stake: Long,
    val num_delegates: Int,
    val service_charge: Double
)
