package com.github.dzivko1.dullcoin.data.core.serial

import com.github.dzivko1.dullcoin.crypto.Crypto
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.security.KeyFactory
import java.security.PublicKey
import java.security.spec.X509EncodedKeySpec

object PublicKeySerializer : KSerializer<PublicKey> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("PublicKey", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: PublicKey) {
        encoder.encodeString(Crypto.toBase64String(value.encoded))
    }

    override fun deserialize(decoder: Decoder): PublicKey {
        val keyBytes = Crypto.fromBase64String(decoder.decodeString())
        return with(KeyFactory.getInstance("RSA")) {
            generatePublic(
                X509EncodedKeySpec(keyBytes)
            )
        }
    }
}