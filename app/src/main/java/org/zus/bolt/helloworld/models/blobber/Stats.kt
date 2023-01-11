package org.zus.bolt.helloworld.models.blobber

data class Stats(
    val latest_closed_challenge: String,
    val num_failed_challenges: Int,
    val num_of_reads: Int,
    val num_of_writes: Int,
    val num_open_challenges: Int,
    val num_success_challenges: Int,
    val total_challenges: Int,
    val used_size: Int
)
