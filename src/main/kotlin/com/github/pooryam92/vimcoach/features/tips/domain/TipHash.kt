package com.github.pooryam92.vimcoach.features.tips.domain

import java.security.MessageDigest

@JvmInline
value class TipHash(val value: String) {
    companion object {
        fun fromTip(tip: VimTip): TipHash {
            return TipHash(sha256(tip.summary.trim()))
        }

        private fun sha256(value: String): String {
            val digest = MessageDigest.getInstance("SHA-256")
                .digest(value.toByteArray(Charsets.UTF_8))
            return digest.joinToString("") { "%02x".format(it) }
        }
    }
}
