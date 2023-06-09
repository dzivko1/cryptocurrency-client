package com.github.dzivko1.dullcoin.crypto

import com.github.dzivko1.dullcoin.util.threadLocal
import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.security.*
import java.util.*

object Crypto {

    init {
        Security.addProvider(BouncyCastleProvider())
    }

    private val sha256 by threadLocal { MessageDigest.getInstance("SHA-256") }
    private val ripemd160 by threadLocal { MessageDigest.getInstance("RIPEMD160") }
    private val keyGen by threadLocal {
        KeyPairGenerator.getInstance("RSA").apply {
            initialize(2048)
        }
    }
    private val signer by threadLocal { Signature.getInstance("SHA256withRSA") }
    private val base64Encoder by threadLocal { Base64.getEncoder() }
    private val base64Decoder by threadLocal { Base64.getDecoder() }

    fun generateKeyPair(): KeyPair = keyGen.generateKeyPair()

    fun toBase64String(byteArray: ByteArray): String = base64Encoder.encodeToString(byteArray)
    fun fromBase64String(string: String): ByteArray = base64Decoder.decode(string)

    fun hash(data: String): String {
        return toBase64String(
            hash(data.toByteArray())
        )
    }

    fun hash(data: ByteArray): ByteArray {
        return sha256.digest(data)
    }

    fun hashShort(data: ByteArray): ByteArray {
        return ripemd160.digest(data)
    }

    fun sign(message: String, privateKey: PrivateKey): String {
        val signature = with(signer) {
            initSign(privateKey)
            update(message.toByteArray())
            sign()
        }
        return toBase64String(signature)
    }

    fun verify(message: String, publicKey: PublicKey, signature: String): Boolean {
        return with(signer) {
            initVerify(publicKey)
            update(message.toByteArray())
            verify(fromBase64String(signature))
        }
    }
}