package com.github.dzivko1.dullcoin.crypto

import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.MessageDigest
import java.security.PrivateKey
import java.security.PublicKey
import java.security.Signature

object Crypto {

    private val messageDigest by lazy { MessageDigest.getInstance("SHA-256") }
    private val keyGen by lazy {
        KeyPairGenerator.getInstance("RSA").apply {
            initialize(2048)
        }
    }
    private val _signer = ThreadLocal.withInitial { Signature.getInstance("SHA256withRSA") }
    private val signer get() = _signer.get()

    fun generateKeyPair(): KeyPair = keyGen.generateKeyPair()

    fun hash(data: String): String {
        return hash(data.encodeToByteArray())
            .decodeToString()
    }

    fun hash(data: ByteArray): ByteArray {
        return messageDigest.digest(data)
    }

    fun sign(message: String, privateKey: PrivateKey): String {
        return with(signer) {
            initSign(privateKey)
            update(message.encodeToByteArray())
            sign().decodeToString()
        }
    }

    fun verify(message: String, publicKey: PublicKey, signature: String): Boolean {
        return with(signer) {
            initVerify(publicKey)
            update(message.encodeToByteArray())
            verify(signature.encodeToByteArray())
        }
    }
}