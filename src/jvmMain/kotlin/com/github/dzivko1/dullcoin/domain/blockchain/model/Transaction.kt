package com.github.dzivko1.dullcoin.domain.blockchain.model

import kotlinx.serialization.Serializable
import java.security.PublicKey

@Serializable
data class Transaction(
    val id: String,
    val senderSignature: String,
    val inputs: List<Input>,
    val outputs: List<Output>
) {
    @Serializable
    data class Input(
        val transactionId: String,
        val outputIndex: Int
    )

    @Serializable
    data class Output(
        val value: Int,
        val recipientKey: PublicKey
    )
}
