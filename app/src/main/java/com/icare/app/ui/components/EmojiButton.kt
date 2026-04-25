package com.icare.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.icare.app.data.model.EmojiCategory
import com.icare.app.data.model.EmojiStatus
import com.icare.app.ui.theme.BadRed
import com.icare.app.ui.theme.HappyGreen
import com.icare.app.ui.theme.LowAmber

@Composable
fun EmojiButton(
    emojiStatus: EmojiStatus,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 100.dp,
    showCategoryBorder: Boolean = false
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Load Lottie composition
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(emojiStatus.lottieRes)
    )

    val scale by animateFloatAsState(
        targetValue = when {
            isPressed -> 0.9f
            isSelected -> 1.1f
            else -> 1.0f
        },
        animationSpec = spring(),
        label = "scale"
    )

    // Border color based on category (for default emojis)
    val borderColor = when (emojiStatus.category) {
        EmojiCategory.POSITIVE -> HappyGreen
        EmojiCategory.NEUTRAL -> LowAmber
        EmojiCategory.NEGATIVE -> BadRed
    }

    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) {
            borderColor.copy(alpha = 0.15f)
        } else if (showCategoryBorder) {
            borderColor.copy(alpha = 0.08f)
        } else {
            Color.Transparent
        },
        label = "bgColor"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(size)
                .scale(scale)
                .clip(CircleShape)
                .then(
                    if (showCategoryBorder) {
                        Modifier.border(2.dp, borderColor.copy(alpha = 0.6f), CircleShape)
                    } else {
                        Modifier
                    }
                )
                .background(backgroundColor)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick
                )
        ) {
            LottieAnimation(
                composition = composition,
                iterations = LottieConstants.IterateForever,
                modifier = Modifier.size(size * 0.7f)
            )
        }

        Text(
            text = emojiStatus.label,
            style = MaterialTheme.typography.labelLarge,
            textAlign = TextAlign.Center,
            color = if (isSelected) {
                borderColor
            } else {
                MaterialTheme.colorScheme.onSurface
            }
        )
    }
}
