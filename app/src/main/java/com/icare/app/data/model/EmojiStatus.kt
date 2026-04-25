package com.icare.app.data.model

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
    val isDefault: Boolean = false
) {
    companion object {
        // Default emojis - one from each category
        val HAPPY = EmojiStatus("happy", "\uD83D\uDE0A", "Happy", EmojiCategory.POSITIVE, isDefault = true)
        val TIRED = EmojiStatus("tired", "\uD83D\uDE34", "Tired", EmojiCategory.NEUTRAL, isDefault = true)
        val FEELING_BAD = EmojiStatus("bad", "\uD83D\uDE22", "Feeling Bad", EmojiCategory.NEGATIVE, isDefault = true)

        // Expanded view emojis - grouped by category (excluding defaults)
        val POSITIVE_EXTRAS = listOf(
            EmojiStatus("excited", "\uD83C\uDF89", "Excited", EmojiCategory.POSITIVE),
            EmojiStatus("peaceful", "\uD83D\uDE0C", "Peaceful", EmojiCategory.POSITIVE),
            EmojiStatus("grateful", "\uD83D\uDE4F", "Grateful", EmojiCategory.POSITIVE)
        )

        val NEUTRAL_EXTRAS = listOf(
            EmojiStatus("hungry", "\uD83C\uDF7D", "Hungry", EmojiCategory.NEUTRAL),
            EmojiStatus("bored", "\uD83D\uDE11", "Bored", EmojiCategory.NEUTRAL),
            EmojiStatus("thinking", "\uD83E\uDD14", "Thinking", EmojiCategory.NEUTRAL)
        )

        val NEGATIVE_EXTRAS = listOf(
            EmojiStatus("angry", "\uD83D\uDE20", "Angry", EmojiCategory.NEGATIVE),
            EmojiStatus("frustrated", "\uD83D\uDE24", "Frustrated", EmojiCategory.NEGATIVE),
            EmojiStatus("sick", "\uD83E\uDD12", "Sick", EmojiCategory.NEGATIVE)
        )

        val PREDEFINED_EXTRAS = POSITIVE_EXTRAS + NEUTRAL_EXTRAS + NEGATIVE_EXTRAS

        val DEFAULTS = listOf(HAPPY, TIRED, FEELING_BAD)

        fun allAvailable(): List<EmojiStatus> = DEFAULTS + PREDEFINED_EXTRAS

        fun shouldNotify(status: EmojiStatus): Boolean {
            return status.category == EmojiCategory.NEGATIVE
        }
    }
}
