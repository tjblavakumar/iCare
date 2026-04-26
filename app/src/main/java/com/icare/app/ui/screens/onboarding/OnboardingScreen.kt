package com.icare.app.ui.screens.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.icare.app.ui.theme.DarkCharcoal
import com.icare.app.ui.theme.MediumGrey
import com.icare.app.ui.theme.SoothingBlue
import com.icare.app.ui.theme.WarmWhite

data class OnboardingPage(
    val emoji: String,
    val title: String,
    val description: String
)

@Composable
fun OnboardingScreen(
    onFinished: () -> Unit
) {
    val pages = listOf(
        OnboardingPage(
            emoji = "\u2764\uFE0F",
            title = "Welcome to iCare",
            description = "Share how you feel with people who care about you. No messages, no pressure \u2014 just a simple tap."
        ),
        OnboardingPage(
            emoji = "\uD83D\uDE0A",
            title = "Tap Your Mood",
            description = "One tap to let your loved ones know how you're doing. Choose from happy, low, or bad \u2014 or pick from more options."
        ),
        OnboardingPage(
            emoji = "\uD83D\uDC65",
            title = "Build Your Circle",
            description = "Add family and friends from your contacts. They'll see your mood and you'll see theirs. It's mutual."
        ),
        OnboardingPage(
            emoji = "\uD83D\uDD14",
            title = "Stay Connected",
            description = "Get notified when someone feels low. If someone hasn't checked in for 48 hours, they'll show in grey \u2014 a gentle nudge to reach out."
        )
    )

    var currentPage by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmWhite)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Skip button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = onFinished) {
                Text(
                    text = "Skip",
                    color = DarkCharcoal,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        AnimatedContent(targetState = currentPage, label = "onboarding") { page ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = pages[page].emoji,
                    fontSize = 96.sp
                )

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = pages[page].title,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = DarkCharcoal
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = pages[page].description,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.Center,
                    color = DarkCharcoal,
                    lineHeight = 28.sp
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Page indicators
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 24.dp)
        ) {
            pages.forEachIndexed { index, _ ->
                Box(
                    modifier = Modifier
                        .size(if (index == currentPage) 12.dp else 8.dp)
                        .clip(CircleShape)
                        .background(
                            if (index == currentPage) SoothingBlue else MediumGrey.copy(alpha = 0.4f)
                        )
                )
            }
        }

        // Next / Get Started button
        Button(
            onClick = {
                if (currentPage < pages.size - 1) {
                    currentPage++
                } else {
                    onFinished()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = SoothingBlue)
        ) {
            Text(
                text = if (currentPage < pages.size - 1) "Next" else "Get Started",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
