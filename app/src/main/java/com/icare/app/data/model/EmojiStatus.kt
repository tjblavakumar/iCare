package com.icare.app.data.model

import com.icare.app.R

enum class EmojiCategory {
    POSITIVE,
    NEGATIVE,
    NEUTRAL
}

data class EmojiStatus(
    val id: String,
    val emoji: String,
    val label: String,
    val category: EmojiCategory,
    val lottieRes: Int,
    val isDefault: Boolean = false
) {
    companion object {
        // Default emojis - one from each category
        val HAPPY = EmojiStatus("happy", "\uD83D\uDE0A", "Happy", EmojiCategory.POSITIVE, R.raw.emoji_happy, isDefault = true)
        val TIRED = EmojiStatus("tired", "\uD83D\uDE34", "Tired", EmojiCategory.NEUTRAL, R.raw.emoji_tired, isDefault = true)
        val FEELING_BAD = EmojiStatus("bad", "\uD83D\uDE22", "Feeling Bad", EmojiCategory.NEGATIVE, R.raw.emoji_bad, isDefault = true)

        // Expanded view emojis - grouped by category (excluding defaults)
        val POSITIVE_EXTRAS = listOf(
            EmojiStatus("excited", "\uD83C\uDF89", "Excited", EmojiCategory.POSITIVE, R.raw.emoji_excited),
            EmojiStatus("peaceful", "\uD83D\uDE0C", "Peaceful", EmojiCategory.POSITIVE, R.raw.emoji_peaceful),
            EmojiStatus("grateful", "\uD83D\uDE4F", "Grateful", EmojiCategory.POSITIVE, R.raw.emoji_grateful)
        )

        val NEUTRAL_EXTRAS = listOf(
            EmojiStatus("hungry", "\uD83E\uDD24", "Hungry", EmojiCategory.NEUTRAL, R.raw.emoji_hungry),
            EmojiStatus("bored", "\uD83D\uDE10", "Bored", EmojiCategory.NEUTRAL, R.raw.emoji_bored),
            EmojiStatus("sleepy", "\uD83D\uDE2A", "Sleepy", EmojiCategory.NEUTRAL, R.raw.emoji_sleepy)
        )

        val NEGATIVE_EXTRAS = listOf(
            EmojiStatus("angry", "\uD83D\uDE20", "Angry", EmojiCategory.NEGATIVE, R.raw.emoji_angry),
            EmojiStatus("frustrated", "\uD83D\uDE24", "Frustrated", EmojiCategory.NEGATIVE, R.raw.emoji_frustrated),
            EmojiStatus("sick", "\uD83E\uDD12", "Sick", EmojiCategory.NEGATIVE, R.raw.emoji_sick)
        )

        val PREDEFINED_EXTRAS = POSITIVE_EXTRAS + NEUTRAL_EXTRAS + NEGATIVE_EXTRAS

        val DEFAULTS = listOf(HAPPY, TIRED, FEELING_BAD)

        fun allAvailable(): List<EmojiStatus> = DEFAULTS + PREDEFINED_EXTRAS

        fun shouldNotify(status: EmojiStatus): Boolean {
            return status.category == EmojiCategory.NEGATIVE
        }
    }
}
