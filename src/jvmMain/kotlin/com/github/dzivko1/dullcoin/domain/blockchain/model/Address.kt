package com.github.dzivko1.dullcoin.domain.blockchain.model

import com.github.dzivko1.dullcoin.crypto.Crypto
import com.github.dzivko1.dullcoin.data.core.serial.PublicKeySerializer
import kotlinx.serialization.Serializable
import java.security.PublicKey

@Serializable
data class Address(
    @Serializable(with = PublicKeySerializer::class)
    val publicKey: PublicKey
) {
    override fun toString(): String {
        return Crypto.toBase64String(publicKey.encoded)
    }
}
