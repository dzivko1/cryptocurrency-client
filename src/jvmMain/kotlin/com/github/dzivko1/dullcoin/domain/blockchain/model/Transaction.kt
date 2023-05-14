package com.github.dzivko1.dullcoin.domain.blockchain.model

import com.github.dzivko1.dullcoin.crypto.Crypto
import com.github.dzivko1.dullcoin.data.core.serial.PublicKeySerializer
import kotlinx.serialization.Serializable
import java.security.PrivateKey
import java.security.PublicKey

@Serializable
class Transaction(
    @Serializable(with = PublicKeySerializer::class)
    val senderPublicKey: PublicKey?,
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
        val amount: Long,
        val recipient: Address
    )

    val id: String get() = hash()

    var senderSignature: String? = null
        private set

    fun hash(): String {
        return Crypto.hash(
            senderPublicKey?.encoded?.let { Crypto.toBase64String(it) } + inputs.joinToString() + outputs.joinToString()
        )
    }

    fun sign(key: PrivateKey) {
        senderSignature = Crypto.sign(hash(), key)
    }
}
