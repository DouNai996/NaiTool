package com.naitool.ui.clickgui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.abs

object ClickGuiColors {
	val backgroundColor = Color(26, 26, 26)
	val titleBgColor = Color(0, 0, 0)
	val moduleHoverBgColor = Color(105, 180, 255)
	val moduleEnabledBgColor = Color(108, 53, 222)
	val settingBgColor = Color(40, 40, 40)
	val settingHoverColor = Color(60, 60, 60)
	val accentColor = Color(108, 53, 222)
	val textColor = Color.White
	val textSecondaryColor = Color.White.copy(alpha = 0.9f)
	val dropdownBgColor = Color(30, 30, 30)
	val sliderTrackColor = Color.Gray.copy(alpha = 0.3f)
	val textFieldBgColor = Color(50, 20, 40)
}

@Composable
fun CustomSwitch(
	checked: Boolean,
	onCheckedChange: (Boolean) -> Unit,
	modifier: Modifier = Modifier,
	width: Dp = 24.dp,
	height: Dp = 12.dp,
	checkedTrackColor: Color = ClickGuiColors.accentColor,
	uncheckedTrackColor: Color = Color.Gray.copy(alpha = 0.4f),
	thumbColor: Color = Color.White
) {
	val thumbPadding = 2.dp
	val thumbSize = height - thumbPadding * 2

	val thumbOffset by animateFloatAsState(
		targetValue = if (checked) 1f else 0f,
		animationSpec = tween(durationMillis = 150)
	)

	val trackColor by animateColorAsState(
		targetValue = if (checked) checkedTrackColor else uncheckedTrackColor,
		animationSpec = tween(durationMillis = 150)
	)

	Box(
		modifier = modifier
			.width(width)
			.height(height)
			.clip(RoundedCornerShape(height / 2))
			.background(trackColor)
			.pointerInput(checked) {
				detectTapGestures { onCheckedChange(!checked) }
			},
		contentAlignment = androidx.compose.ui.Alignment.CenterStart
	) {
		Box(
			modifier = Modifier
				.padding(thumbPadding)
				.offset {
					val maxOffset = (width - thumbSize - thumbPadding * 2).toPx()
					IntOffset((thumbOffset * maxOffset).toInt(), 0)
				}
				.size(thumbSize)
				.clip(RoundedCornerShape(50))
				.background(thumbColor)
		)
	}
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun CustomSlider(
	value: Float,
	onValueChange: (Float) -> Unit,
	onValueChangeFinished: () -> Unit,
	valueRange: ClosedFloatingPointRange<Float>,
	modifier: Modifier = Modifier,
	trackHeight: Dp = 4.dp,
	thumbSize: Dp = 10.dp,
	activeColor: Color = ClickGuiColors.accentColor,
	inactiveColor: Color = ClickGuiColors.sliderTrackColor
) {
	var sliderWidth by remember { mutableStateOf(0f) }
	var isHovered by remember { mutableStateOf(false) }
	val span = (valueRange.endInclusive - valueRange.start).coerceAtLeast(0.0001f)
	val fraction = ((value - valueRange.start) / span).coerceIn(0f, 1f)

	val thumbScale by animateFloatAsState(
		targetValue = if (isHovered) 1.3f else 1f,
		animationSpec = tween(durationMillis = 150)
	)

	Box(
		modifier = modifier
			.onPointerEvent(PointerEventType.Enter) { isHovered = true }
			.onPointerEvent(PointerEventType.Exit) { isHovered = false }
			.pointerInput(valueRange) {
				detectTapGestures { offset ->
					if (sliderWidth > 0f) {
						val newFraction = (offset.x / sliderWidth).coerceIn(0f, 1f)
						onValueChange(valueRange.start + newFraction * span)
						onValueChangeFinished()
					}
				}
			}
			.pointerInput(valueRange) {
				detectHorizontalDragGestures(
					onDragEnd = { onValueChangeFinished() },
					onHorizontalDrag = { change, _ ->
						change.consume()
						if (sliderWidth > 0f) {
							val x = change.position.x.coerceIn(0f, sliderWidth)
							val newFraction = x / sliderWidth
							onValueChange(valueRange.start + newFraction * span)
						}
					}
				)
			},
		contentAlignment = androidx.compose.ui.Alignment.CenterStart
	) {
		Box(
			modifier = Modifier
				.fillMaxWidth()
				.height(trackHeight)
				.background(inactiveColor, RoundedCornerShape(trackHeight / 2))
				.onGloballyPositioned { sliderWidth = it.size.width.toFloat() }
		)

		Box(
			modifier = Modifier
				.fillMaxWidth(fraction)
				.height(trackHeight)
				.background(activeColor, RoundedCornerShape(trackHeight / 2))
		)

		Box(
			modifier = Modifier
				.offset {
					IntOffset(
						(fraction * sliderWidth).toInt() - (thumbSize.toPx() * thumbScale / 2).toInt(),
						0
					)
				}
				.size(thumbSize * thumbScale)
				.background(activeColor, RoundedCornerShape(50))
		)
	}
}

/** 色相位（0..360）转 RGB（0..255） */
fun hueToRgb(hue: Float): Triple<Float, Float, Float> {
	val c = 1f
	val x = c * (1 - abs((hue / 60f) % 2 - 1))
	val (r, g, b) = when {
		hue < 60 -> Triple(c, x, 0f)
		hue < 120 -> Triple(x, c, 0f)
		hue < 180 -> Triple(0f, c, x)
		hue < 240 -> Triple(0f, x, c)
		hue < 300 -> Triple(x, 0f, c)
		else -> Triple(c, 0f, x)
	}
	return Triple(r * 255, g * 255, b * 255)
}
