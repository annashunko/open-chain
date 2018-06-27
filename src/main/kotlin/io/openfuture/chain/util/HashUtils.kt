package io.openfuture.chain.util

import org.apache.commons.lang3.StringUtils
import org.bouncycastle.crypto.digests.RIPEMD160Digest
import java.security.MessageDigest

object HashUtils {

    private const val SHA256 = "SHA-256"

    fun generateHash(bytes: ByteArray) = generateHashBytes(bytes).fold(StringUtils.EMPTY) { str, it -> str + "%02x".format(it) }

    fun generateHashBytes(bytes: ByteArray): ByteArray = MessageDigest.getInstance(SHA256).digest(bytes)

    fun keyHash(bytes: ByteArray): ByteArray {
        val result = ByteArray(20)
        val sha256 = MessageDigest.getInstance(SHA256).digest(bytes)
        val digest = RIPEMD160Digest()
        digest.update(sha256, 0, sha256.size)
        digest.doFinal(result, 0)
        return result
    }

}
