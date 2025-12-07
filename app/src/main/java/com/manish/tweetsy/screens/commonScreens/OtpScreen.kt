package com.manish.tweetsy.screens.commonScreens

import android.util.Log
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.manish.tweetsy.R
import kotlinx.coroutines.delay

@Preview
@Composable
fun OtpInput(
    otpLength: Int = 6,
    onOtpFilled: (String) -> Unit = {}
) {
    var otpStack by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    // Current active index (next position to fill)
    val activeIndex by remember {
        derivedStateOf { otpStack.length }
    }

    // Auto-focus on load
    LaunchedEffect(Unit) {
        delay(500)
        focusRequester.requestFocus()
        keyboardController?.show()
    }

    // Auto-trigger callback when OTP is complete (but don't hide keyboard)
    LaunchedEffect(otpStack) {
        if (otpStack.length == otpLength) {
            delay(200)
            onOtpFilled(otpStack)
            // Keyboard stays open - user can still backspace and edit
        }
    }

    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Display boxes
        repeat(otpLength) { index ->
            val digit = if (index < otpStack.length) otpStack[index].toString() else ""
            val isActive = index == activeIndex

            Box(
                modifier = Modifier
                    .weight(1f)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = {
                                // Force request focus and show keyboard
                                focusRequester.requestFocus()
                                keyboardController?.show()
                            }
                        )
                    }
            ) {
                OutlinedTextField(
                    value = digit,
                    onValueChange = { }, // Disabled - handled by invisible field
                    enabled = false, // Make it non-editable
                    modifier = Modifier
                        .size(56.dp),
                    textStyle = LocalTextStyle.current.copy(
                        fontSize = 24.sp,
                        textAlign = TextAlign.Center,
                        color = colorResource(R.color.primary400)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledBorderColor = if (digit.isNotEmpty() || isActive) {
                            colorResource(R.color.primary400)
                        } else {
                            colorResource(R.color.neutral400)
                        },
                        disabledTextColor = colorResource(R.color.primary400),
                        disabledContainerColor = if (digit.isNotEmpty() || isActive) {
                            colorResource(R.color.TF_bg_focus)
                        } else {
                            Color.White
                        }
                    ),
                )
            }
        }
    }

    // Invisible field that handles all input - STACK LOGIC (Always active)
    OutlinedTextField(
        value = otpStack,
        onValueChange = { newValue ->
            val digits = newValue.filter { it.isDigit() }

            when {
                // Handle paste of full OTP
                digits.length >= otpLength -> {
                    otpStack = digits.take(otpLength)
                }

                // Handle normal input (push to stack)
                digits.length > otpStack.length -> {
                    otpStack = digits.take(otpLength)
                }

                // Handle backspace (pop from stack)
                digits.length < otpStack.length -> {
                    otpStack = digits
                }
            }
        },
        modifier = Modifier
            .size(1.dp) // Very small but not 0
            .focusRequester(focusRequester),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent,
            cursorColor = Color.Transparent
        )
    )
}

@Composable
fun OtpTimerWithProgress(
    totalSeconds: Int = 120,
    onResendClick: () -> Unit = {}
) {
    var remaining by remember { mutableIntStateOf(totalSeconds) }
    var canResend by remember { mutableStateOf(false) }

    LaunchedEffect(remaining) {
        if (remaining > 0) {
            delay(1000)
            remaining--
        } else {
            canResend = true
        }
    }

    val progress = if (totalSeconds > 0) remaining / totalSeconds.toFloat() else 0f
    val minutes = remaining / 60
    val seconds = remaining % 60

    Column {
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(5.dp)),
            color = Color(0xFF12D6C4),
            trackColor = Color(0xFFE0E0E0)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (canResend) {
                Text(
                    text = "Didn't receive code?",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
                Text(
                    text = "RESEND",
                    color = Color(0xFF12D6C4),
                    fontSize = 14.sp,
                    modifier = Modifier.padding(start = 4.dp)
                )
            } else {
                Text(
                    text = "Code expires in ${minutes}:${seconds.toString().padStart(2, '0')}",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
                Text(
                    text = "%02d:%02d".format(minutes, seconds),
                    color = Color(0xFF12D6C4),
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun OtpScreen() {
    Column(
        modifier = Modifier
            .padding(vertical = 12.dp)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OtpInput(
            onOtpFilled = { otp ->
                // OTP is ready - Call verify API

                Log.d("","OTP Filled: $otp")
                // TODO: Call your verification API here
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        OtpTimerWithProgress(
            totalSeconds = 120, // 2 minutes for OTP expiry
            onResendClick = {

                Log.d("","Resend OTP clicked")
                // Reset remaining seconds and enable resend
                // TODO: Call resend OTP API
            }
        )
    }
}