package com.example.ui.util

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.CyanPrimary

/**
 * Reusable D-Pad focus modifier for Android TV remote navigation.
 * Highlights focused elements with a glowing border and subtle spring bounce scale animation (1.05x),
 * provides visual feedback via elevation and border, and handles D-Pad Center / Enter key triggers.
 */
@Composable
fun Modifier.dpadFocusable(
    shape: CornerBasedShape = RoundedCornerShape(12.dp),
    focusedBorderColor: Color = CyanPrimary,
    focusedBorderWidth: Dp = 2.5.dp,
    scaleOnFocus: Float = 1.05f,
    onClick: (() -> Unit)? = null,
    onFocusChange: ((Boolean) -> Unit)? = null
): Modifier {
    var isFocused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isFocused) scaleOnFocus else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "dpadScale"
    )

    var modifier = this
        .onFocusChanged { focusState ->
            isFocused = focusState.isFocused
            onFocusChange?.invoke(focusState.isFocused)
        }
        .focusable()
        .scale(scale)

    if (isFocused) {
        modifier = modifier
            .shadow(elevation = 10.dp, shape = shape, spotColor = focusedBorderColor, ambientColor = focusedBorderColor)
            .border(BorderStroke(focusedBorderWidth, focusedBorderColor), shape = shape)
    }

    if (onClick != null) {
        modifier = modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .onKeyEvent { keyEvent ->
                when (keyEvent.key) {
                    Key.DirectionCenter, Key.Enter, Key.NumPadEnter, Key.Spacebar -> {
                        if (keyEvent.type == KeyEventType.KeyUp) {
                            onClick.invoke()
                        }
                        true
                    }
                    else -> false
                }
            }
    }

    return modifier
}
