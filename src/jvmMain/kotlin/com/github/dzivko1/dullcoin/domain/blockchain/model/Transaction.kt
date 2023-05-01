package com.github.dzivko1.dullcoin.domain.blockchain.model

import com.github.dzivko1.dullcoin.crypto.Crypto
import kotlinx.serialization.Serializable
import java.security.PrivateKey

@Serializable
class Transaction(
    val sender: Address?,
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
        val amount: Int,
        val recipient: Address
    )

    val id: String get() = hash()

    var senderSignature: String? = null
        private set

    fun hash(): String {
        return Crypto.hash(
            sender.toString() + inputs.joinToString() + outputs.joinToString()
        )
    }

    fun sign(key: PrivateKey) {
        senderSignature = Crypto.sign(hash(), key)
    }
}
