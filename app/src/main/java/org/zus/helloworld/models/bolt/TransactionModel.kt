package org.zus.helloworld.models.bolt

data class TransactionModel(
    val CreatedAt: String,
    val DeletedAt: Any,
    val ID: Long,
    val UpdatedAt: String,
    val block_hash: String,
    val client_id: String,
    val creation_date: Long,
    val fee: Long,
    val hash: String,
    val nonce: Long,
    val output_hash: String,
    val round: Long,
    val signature: String,
    val status: Long,
    val to_client_id: String,
    val transaction_data: String,
    val transaction_output: String,
    val transaction_type: Long,
    val value: Long,
    val version: String,
)
