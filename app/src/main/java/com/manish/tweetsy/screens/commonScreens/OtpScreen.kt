package com.manish.tweetsy.screens.commonScreens

import android.util.Log
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
    totalSeconds: Int = 7200,
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
        )
        {
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
                    text = " code Expires in 2 hours",
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
            onResendClick = {

                Log.d("","Resend OTP clicked")
                // Reset remaining seconds and enable resend
                // TODO: Call resend OTP API
            }
        )
    }
}


@Preview
@Composable
fun OtpScreenPreview() {
    OtpScreen()
}


//-------- Fifth version ------------------
/*

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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thpl.common.R
import kotlinx.coroutines.delay
import timber.log.Timber

@Preview
@Composable
fun OtpInput(
    otpLength: Int = 6,
    onOtpFilled: (String) -> Unit = {}
) {
    var otpStack by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    // Current active index (next position to fill)
    val activeIndex by remember {
        derivedStateOf { otpStack.length }
    }

    // Auto-focus on load and keep keyboard open
    LaunchedEffect(Unit) {
        delay(500) // Increased delay to ensure layout is ready
        focusRequester.requestFocus()
    }

    // Keep focus always - even after OTP complete
    LaunchedEffect(activeIndex) {
        focusRequester.requestFocus()
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

            OutlinedTextField(
                value = digit,
                onValueChange = { }, // Disabled - handled by invisible field
                enabled = false, // Make it non-clickable
                modifier = Modifier
                    .weight(1f)
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
                Timber.d("OTP Filled: $otp")
                // TODO: Call your verification API here
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        OtpTimerWithProgress(
            totalSeconds = 120, // 2 minutes for OTP expiry
            onResendClick = {
                Timber.d("Resend OTP clicked")
                // TODO: Call resend OTP API
            }
        )
    }
}*/


//-----------fourth version -----------------------

/*

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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thpl.common.R
import kotlinx.coroutines.delay
import timber.log.Timber

@Preview
@Composable
fun OtpInput(
    otpLength: Int = 6,
    onOtpFilled: (String) -> Unit = {}
) {
    var otpStack by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    // Current active index (next position to fill)
    val activeIndex by remember {
        derivedStateOf { otpStack.length }
    }

    // Auto-focus on load and keep keyboard open
    LaunchedEffect(Unit) {
        delay(500) // Increased delay to ensure layout is ready
        focusRequester.requestFocus()
    }

    // Keep focus on the active field
    LaunchedEffect(activeIndex) {
        if (activeIndex < otpLength) {
            focusRequester.requestFocus()
        }
    }

    // Auto-trigger callback when OTP is complete
    LaunchedEffect(otpStack) {
        if (otpStack.length == otpLength) {
            delay(200)
            onOtpFilled(otpStack)
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

            OutlinedTextField(
                value = digit,
                onValueChange = { }, // Disabled - handled by invisible field
                enabled = false, // Make it non-clickable
                modifier = Modifier
                    .weight(1f)
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

    // Invisible field that handles all input - STACK LOGIC
    if (activeIndex < otpLength) {
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
                Timber.d("OTP Filled: $otp")
                // TODO: Call your verification API here
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        OtpTimerWithProgress(
            totalSeconds = 120, // 2 minutes for OTP expiry
            onResendClick = {
                Timber.d("Resend OTP clicked")
                // TODO: Call resend OTP API
            }
        )
    }
}*/


//------------------Third version ------------------

/*

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thpl.common.R
import kotlinx.coroutines.delay
import timber.log.Timber

@Preview
@Composable
fun OtpInput(
    otpLength: Int = 6,
    onOtpFilled: (String) -> Unit = {}
) {
    var otp by remember { mutableStateOf(List(otpLength) { "" }) }
    val focusRequesters = remember { List(otpLength) { FocusRequester() } }
    val focusManager = LocalFocusManager.current

    // Auto-focus on first field when component loads
    LaunchedEffect(Unit) {
        delay(300) // Small delay for smooth animation
        focusRequesters[0].requestFocus()
    }

    // Auto-trigger callback when OTP is complete
    LaunchedEffect(otp) {
        if (otp.all { it.isNotEmpty() }) {
            val otpString = otp.joinToString("")
            delay(200) // Small delay for better UX
            onOtpFilled(otpString)
            focusManager.clearFocus()
        }
    }

    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        otp.forEachIndexed { index, digit ->
            OutlinedTextField(
                value = digit,
                onValueChange = { newValue ->
                    when {
                        // Handle paste of full OTP
                        newValue.length >= otpLength -> {
                            val digits = newValue.filter { it.isDigit() }.take(otpLength)
                            if (digits.length == otpLength) {
                                otp = digits.map { it.toString() }
                            }
                        }

                        // Handle paste of partial digits
                        newValue.length > 1 -> {
                            val digits = newValue.filter { it.isDigit() }

                            // If it looks like full OTP, fill from start
                            if (digits.length == otpLength) {
                                otp = digits.map { it.toString() }
                            } else if (digits.isNotEmpty()) {
                                // Partial paste - fill from current position
                                val newOtp = otp.toMutableList()
                                digits.forEachIndexed { i, char ->
                                    val targetIndex = index + i
                                    if (targetIndex < otpLength) {
                                        newOtp[targetIndex] = char.toString()
                                    }
                                }
                                otp = newOtp

                                // Move to next empty field
                                val nextEmpty = newOtp.indexOfFirst { it.isEmpty() }
                                if (nextEmpty != -1) {
                                    focusRequesters[nextEmpty].requestFocus()
                                }
                            }
                        }

                        // Handle single digit - Zomato/Blinkit style behavior
                        newValue.length == 1 && newValue.first().isDigit() -> {
                            otp = otp.toMutableList().also { it[index] = newValue }

                            // Auto-move to next field
                            if (index < otpLength - 1) {
                                focusRequesters[index + 1].requestFocus()
                            }
                        }

                        // Handle backspace - Zomato/Blinkit style
                        newValue.isEmpty() -> {
                            if (digit.isNotEmpty()) {
                                // Clear current field, move to previous
                                otp = otp.toMutableList().also { it[index] = "" }
                                if (index > 0) {
                                    focusRequesters[index - 1].requestFocus()
                                }
                            } else if (index > 0) {
                                // Already empty, clear previous and move there
                                otp = otp.toMutableList().also { it[index - 1] = "" }
                                focusRequesters[index - 1].requestFocus()
                            }
                        }
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .size(56.dp)
                    .focusRequester(focusRequesters[index]),
                textStyle = LocalTextStyle.current.copy(
                    fontSize = 24.sp,
                    textAlign = TextAlign.Center,
                    color = colorResource(R.color.primary400)
                ),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.NumberPassword,
                    imeAction = if (index == otpLength - 1) ImeAction.Done else ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        if (otp.all { it.isNotEmpty() }) {
                            focusManager.clearFocus()
                            onOtpFilled(otp.joinToString(""))
                        }
                    }
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colorResource(R.color.primary400),
                    unfocusedBorderColor = if (digit.isNotEmpty()) {
                        colorResource(R.color.primary400)
                    } else {
                        colorResource(R.color.neutral400)
                    },
                    cursorColor = colorResource(R.color.primary400),
                    focusedContainerColor = colorResource(R.color.TF_bg_focus),
                    unfocusedContainerColor = if (digit.isNotEmpty()) {
                        colorResource(R.color.TF_bg_focus)
                    } else {
                        Color.White
                    }
                ),
            )
        }
    }
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
                Timber.d("OTP Filled: $otp")
                // TODO: Call your verification API here
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        OtpTimerWithProgress(
            totalSeconds = 120, // 2 minutes for OTP expiry
            onResendClick = {
                Timber.d("Resend OTP clicked")
                // TODO: Call resend OTP API
            }
        )
    }
}*/

// ---------------------second version -----------------------

/*

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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thpl.common.R
import kotlinx.coroutines.delay
import timber.log.Timber

@Preview
@Composable
fun OtpInput(
    otpLength: Int = 6,
    onOtpFilled: (String) -> Unit = {}
) {
    var otp by remember { mutableStateOf(List(otpLength) { "" }) }
    val focusRequesters = remember { List(otpLength) { FocusRequester() } }
    val focusManager = LocalFocusManager.current

    // Auto-trigger callback when OTP is complete
    LaunchedEffect(otp) {
        if (otp.all { it.isNotEmpty() }) {
            val otpString = otp.joinToString("")
            onOtpFilled(otpString)
            // Clear focus when OTP is complete
            delay(100)
            focusManager.clearFocus()
        }
    }

    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        otp.forEachIndexed { index, digit ->
            OutlinedTextField(
                value = digit,
                onValueChange = { newValue ->
                    val oldValue = digit

                    when {
                        // Handle paste - detect if it's a full OTP (6 digits)
                        newValue.length >= otpLength -> {
                            val digits = newValue.filter { it.isDigit() }.take(otpLength)

                            if (digits.length == otpLength) {
                                // Full OTP pasted - fill all fields from start
                                otp = digits.map { it.toString() }
                            }
                        }

                        // Handle paste of multiple digits but less than full OTP
                        newValue.length > 1 -> {
                            val digits = newValue.filter { it.isDigit() }

                            // Check if user pasted full OTP anywhere
                            if (digits.length == otpLength) {
                                otp = digits.map { it.toString() }
                            } else if (digits.isNotEmpty()) {
                                // Partial paste - fill from current position
                                val newOtp = otp.toMutableList()
                                digits.forEachIndexed { i, char ->
                                    val targetIndex = index + i
                                    if (targetIndex < otpLength) {
                                        newOtp[targetIndex] = char.toString()
                                    }
                                }
                                otp = newOtp

                                // Move focus to next empty or last filled
                                val nextEmptyIndex = newOtp.indexOfFirst { it.isEmpty() }
                                if (nextEmptyIndex != -1 && nextEmptyIndex < otpLength) {
                                    focusRequesters[nextEmptyIndex].requestFocus()
                                }
                            }
                        }

                        // Handle single digit input (typing new digit)
                        newValue.length == 1 && newValue.first().isDigit() -> {
                            otp = otp.toMutableList().also { it[index] = newValue }

                            // Move to next field if not last
                            if (index < otpLength - 1) {
                                focusRequesters[index + 1].requestFocus()
                            }
                        }

                        // Handle backspace - when field becomes empty
                        newValue.isEmpty() && oldValue.isNotEmpty() -> {
                            // Clear current field
                            otp = otp.toMutableList().also { it[index] = "" }

                            // Move to previous field
                            if (index > 0) {
                                focusRequesters[index - 1].requestFocus()
                            }
                        }

                        // Handle backspace on already empty field
                        newValue.isEmpty() && oldValue.isEmpty() -> {
                            if (index > 0) {
                                // Clear previous field and move there
                                otp = otp.toMutableList().also { it[index - 1] = "" }
                                focusRequesters[index - 1].requestFocus()
                            }
                        }
                    }
                },
                modifier = Modifier
                    .weight(1f, true)
                    .size(60.dp)
                    .focusRequester(focusRequesters[index]),
                textStyle = LocalTextStyle.current.copy(
                    fontSize = 22.sp,
                    textAlign = TextAlign.Center
                ),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colorResource(R.color.primary400),
                    unfocusedBorderColor = colorResource(R.color.neutral400),
                    cursorColor = colorResource(R.color.primary400),
                    focusedContainerColor = colorResource(R.color.TF_bg_focus),
                    unfocusedContainerColor = if (digit.isNotEmpty()) {
                        colorResource(R.color.TF_bg_focus)
                    } else {
                        Color.White
                    }
                ),
            )
        }
    }
}

@Composable
fun OtpTimerWithProgress(
    totalSeconds: Int = 120,
) {
    var remaining by remember { mutableIntStateOf(totalSeconds) }

    LaunchedEffect(Unit) {
        while (remaining > 0) {
            delay(1000)
            remaining--
        }
    }

    val progress = remaining / totalSeconds.toFloat()
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

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Code expires in 2 hours", color = Color.Gray)

            Text(
                text = "%02d:%02d".format(minutes, seconds),
                color = Color(0xFF12D6C4)
            )
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
            onOtpFilled = {
                // OTP is ready - Call verify API
                Timber.d("OTP: $it")
            }
        )

        Spacer(modifier = Modifier.height(20.dp))

        OtpTimerWithProgress(totalSeconds = 7200)  // 2 hours
    }
}*/

//---- first version ----------------------------------------
/*


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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thpl.common.R
import kotlinx.coroutines.delay
import timber.log.Timber

@Preview
@Composable
fun OtpInput(
    otpLength: Int = 6,
    onOtpFilled: (String) -> Unit = {}
) {
    var otp by remember { mutableStateOf(List(otpLength) { "" }) }
    val focusRequesters = remember { List(otpLength) { FocusRequester() } }
    val focusManager = LocalFocusManager.current

    // Auto-trigger callback when OTP is complete
    LaunchedEffect(otp) {
        if (otp.all { it.isNotEmpty() }) {
            val otpString = otp.joinToString("")
            onOtpFilled(otpString)
            // Clear focus when OTP is complete
            delay(100)
            focusManager.clearFocus()
        }
    }

    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        otp.forEachIndexed { index, digit ->
            OutlinedTextField(
                value = digit,
                onValueChange = { newValue ->
                    when {
                        // Handle paste - detect if it's a full OTP (6 digits)
                        newValue.length >= otpLength -> {
                            val digits = newValue.filter { it.isDigit() }.take(otpLength)

                            if (digits.length == otpLength) {
                                // Full OTP pasted - fill all fields from start
                                otp = digits.map { it.toString() }
                            }
                        }

                        // Handle paste of multiple digits but less than full OTP
                        newValue.length > 1 -> {
                            val digits = newValue.filter { it.isDigit() }

                            // Check if user pasted full OTP anywhere
                            if (digits.length == otpLength) {
                                otp = digits.map { it.toString() }
                            } else if (digits.isNotEmpty()) {
                                // Partial paste - fill from current position
                                val newOtp = otp.toMutableList()
                                digits.forEachIndexed { i, char ->
                                    val targetIndex = index + i
                                    if (targetIndex < otpLength) {
                                        newOtp[targetIndex] = char.toString()
                                    }
                                }
                                otp = newOtp

                                // Move focus to next empty or last filled
                                val nextEmptyIndex = newOtp.indexOfFirst { it.isEmpty() }
                                if (nextEmptyIndex != -1 && nextEmptyIndex < otpLength) {
                                    focusRequesters[nextEmptyIndex].requestFocus()
                                }
                            }
                        }

                        // Handle single digit input
                        newValue.length == 1 && newValue.first().isDigit() -> {
                            otp = otp.toMutableList().also { it[index] = newValue }

                            // Move to next field if not last
                            if (index < otpLength - 1) {
                                focusRequesters[index + 1].requestFocus()
                            }
                        }

                        // Handle deletion/backspace
                        newValue.isEmpty() -> {
                            if (digit.isNotEmpty()) {
                                // Current field has digit - clear it and stay here
                                otp = otp.toMutableList().also { it[index] = "" }
                            } else if (index > 0) {
                                // Current field is empty - clear previous and move there
                                otp = otp.toMutableList().also { it[index - 1] = "" }
                                focusRequesters[index - 1].requestFocus()
                            }
                        }
                    }
                },
                modifier = Modifier
                    .weight(1f, true)
                    .size(60.dp)
                    .focusRequester(focusRequesters[index]),
                textStyle = LocalTextStyle.current.copy(
                    fontSize = 22.sp,
                    textAlign = TextAlign.Center
                ),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colorResource(R.color.primary400),
                    unfocusedBorderColor = colorResource(R.color.neutral400),
                    cursorColor = colorResource(R.color.primary400),
                    focusedContainerColor = colorResource(R.color.TF_bg_focus),
                    unfocusedContainerColor = if (digit.isNotEmpty()) {
                        colorResource(R.color.TF_bg_focus)
                    } else {
                        Color.White
                    }
                ),
            )
        }
    }
}

@Composable
fun OtpTimerWithProgress(
    totalSeconds: Int = 120,
) {
    var remaining by remember { mutableIntStateOf(totalSeconds) }

    LaunchedEffect(Unit) {
        while (remaining > 0) {
            delay(1000)
            remaining--
        }
    }

    val progress = remaining / totalSeconds.toFloat()
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

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Code expires in 2 hours", color = Color.Gray)

            Text(
                text = "%02d:%02d".format(minutes, seconds),
                color = Color(0xFF12D6C4)
            )
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
            onOtpFilled = {
                // OTP is ready - Call verify API
                Timber.d("OTP: $it")
            }
        )

        Spacer(modifier = Modifier.height(20.dp))

        OtpTimerWithProgress(totalSeconds = 7200)  // 2 hours
    }
}*/