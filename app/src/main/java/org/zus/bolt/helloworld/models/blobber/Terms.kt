package org.zus.bolt.helloworld.models.blobber

data class Terms(
    val max_offer_duration: Long,
    val min_lock_demand: Double,
    val read_price: Int,
    val write_price: Int
)
