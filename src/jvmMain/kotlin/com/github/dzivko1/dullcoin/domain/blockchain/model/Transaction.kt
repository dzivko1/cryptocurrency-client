package com.github.dzivko1.dullcoin.domain.blockchain.model

import com.github.dzivko1.dullcoin.crypto.Crypto
import kotlinx.serialization.Serializable
import java.security.PrivateKey
import java.security.PublicKey

@Serializable
data class Transaction(
    val senderKey: PublicKey,
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

    val id: String get() = hash()

    var senderSignature: String? = null
        private set

    fun hash(): String {
        return Crypto.hash(toString())
    }

    fun sign(key: PrivateKey) {
        senderSignature = Crypto.sign(hash(), key)
    }
}
