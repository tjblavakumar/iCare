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
        val HAPPY = EmojiStatus("happy", "\uD83D\uDE0A", "Happy", EmojiCategory.POSITIVE, isDefault = true)
        val FEELING_LOW = EmojiStatus("low", "\uD83D\uDE14", "Feeling Low", EmojiCategory.NEGATIVE, isDefault = true)
        val FEELING_BAD = EmojiStatus("bad", "\uD83D\uDE22", "Feeling Bad", EmojiCategory.NEGATIVE, isDefault = true)

        val PREDEFINED_EXTRAS = listOf(
            EmojiStatus("tired", "\uD83D\uDE34", "Tired", EmojiCategory.NEUTRAL),
            EmojiStatus("excited", "\uD83C\uDF89", "Excited", EmojiCategory.POSITIVE),
            EmojiStatus("angry", "\uD83D\uDE24", "Frustrated", EmojiCategory.NEGATIVE),
            EmojiStatus("sick", "\uD83E\uDD12", "Sick", EmojiCategory.NEGATIVE),
            EmojiStatus("peaceful", "\uD83D\uDE0C", "Peaceful", EmojiCategory.POSITIVE),
            EmojiStatus("anxious", "\uD83D\uDE30", "Anxious", EmojiCategory.NEGATIVE),
            EmojiStatus("grateful", "\uD83D\uDE4F", "Grateful", EmojiCategory.POSITIVE),
            EmojiStatus("bored", "\uD83D\uDE11", "Bored", EmojiCategory.NEUTRAL)
        )

        val DEFAULTS = listOf(HAPPY, FEELING_LOW, FEELING_BAD)

        fun allAvailable(): List<EmojiStatus> = DEFAULTS + PREDEFINED_EXTRAS

        fun shouldNotify(status: EmojiStatus): Boolean {
            return status.category == EmojiCategory.NEGATIVE
        }
    }
}
