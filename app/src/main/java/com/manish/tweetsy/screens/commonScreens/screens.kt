package com.manish.tweetsy.screens.commonScreens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun DotLoader() {
    // This state handles the rotation of the refresh icon.
    val degree by produceState(initialValue = 0) {
        while (true) {
            delay(16) // ~60 frames per second
            value = (value + 5) % 360
        }
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize(fraction = 1f)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Image(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Loading spinner",
                modifier = Modifier
                    .size(60.dp)
                    .rotate(degree.toFloat())
            )

            // NEW: Bouncing dots animation
            BouncingDotsLoading()
        }
    }
}


@Composable
private fun BouncingDotsLoading() {
    val dots = listOf(
        remember { Animatable(0f) },
        remember { Animatable(0f) },
        remember { Animatable(0f) },
    )

    // Launch a coroutine for each dot to animate it
    dots.forEachIndexed { index, animatable ->
        LaunchedEffect(animatable) {
            // Stagger the start of each animation
            delay(index * 200L)
            animatable.animateTo(
                targetValue = -25f, // Jump up by 25 pixels
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 400, easing = LinearOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse // After jumping up, it will come back down
                )
            )
        }
    }

    // Composable layout for the dots
    Row(
        modifier = Modifier.padding(top = 16.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Loading",
            modifier = Modifier.padding(end = 4.dp) // Space between "Loading" and the dots
        )

        dots.forEach { animatable ->
            Text(
                text = ".",
                modifier = Modifier
                    .offset {
                        // Use the animated value for the vertical offset
                        IntOffset(x = 0, y = animatable.value.toInt())
                    }
            )
        }
    }
}