package com.github.dzivko1.dullcoin.domain.blockchain.model

import com.github.dzivko1.dullcoin.crypto.Crypto
import com.github.dzivko1.dullcoin.data.util.encodeToBase58WithChecksum
import kotlinx.serialization.Serializable
import java.security.PublicKey

@Serializable
data class Address(
    val stringRepresentation: String
) {
    constructor(publicKey: PublicKey) : this(toStringRepresentation(publicKey))
}

private fun toStringRepresentation(publicKey: PublicKey): String {
    val shortHash = Crypto.hashShort(Crypto.hash(publicKey.encoded))
    return shortHash.encodeToBase58WithChecksum()
}
