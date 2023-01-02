package org.zus.bolt.helloworld.models

data class TransactionModel(
    val hash: String,
    val from: String,
    val to: String,
    val value: Double,
)
