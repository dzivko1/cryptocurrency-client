package com.github.dzivko1.dullcoin.crypto

import com.github.dzivko1.dullcoin.util.threadLocal
import java.security.*
import java.util.*

object Crypto {

    private val messageDigest by threadLocal { MessageDigest.getInstance("SHA-256") }
    private val keyGen by threadLocal {
        KeyPairGenerator.getInstance("RSA").apply {
            initialize(2048)
        }
    }
    private val signer by threadLocal { Signature.getInstance("SHA256withRSA") }
    private val base64Encoder by threadLocal { Base64.getEncoder() }
    private val base64Decoder by threadLocal { Base64.getDecoder() }

    fun generateKeyPair(): KeyPair = keyGen.generateKeyPair()

    fun hash(data: String): String {
        return base64Encoder.encodeToString(
            hash(data.toByteArray())
        )
    }

    fun hash(data: ByteArray): ByteArray {
        return messageDigest.digest(data)
    }

    fun sign(message: String, privateKey: PrivateKey): String {
        val signature = with(signer) {
            initSign(privateKey)
            update(message.toByteArray())
            sign()
        }
        return base64Encoder.encodeToString(signature)
    }

    fun verify(message: String, publicKey: PublicKey, signature: String): Boolean {
        return with(signer) {
            initVerify(publicKey)
            update(message.toByteArray())
            verify(base64Decoder.decode(signature))
        }
    }
}