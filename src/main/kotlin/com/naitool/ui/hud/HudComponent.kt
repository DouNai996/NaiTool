package com.naitool.ui.hud

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.round
import kotlin.math.roundToInt

/** 组件锚点角；位置比例相对于各自的角（TL=左上，TR=右上） */
enum class HudAnchor { TOP_LEFT, TOP_RIGHT }

@Composable
fun HudComponent(
	id: String,
	anchor: HudAnchor,
	interactive: Boolean,
	default: StoredHudPos = StoredHudPos(),
	content: @Composable () -> Unit
) {
	val density = LocalDensity.current
	val pos = remember { HudEditorManager.stateFor(id, default) }
	val editMode = HudEditorManager.editMode
	val selected = HudEditorManager.selectedId == id

	var baseWidth by remember { mutableFloatStateOf(0f) }
	var baseHeight by remember { mutableFloatStateOf(0f) }

	val scaleAnim by animateFloatAsState(targetValue = pos.scale, tween(120), label = "hudScale")
	val borderColor by animateColorAsState(
		targetValue = when {
			selected && editMode -> Color.Cyan
			editMode -> Color.White.copy(alpha = 0.3f)
			else -> Color.Transparent
		},
		tween(120), label = "hudBorder"
	)

	BoxWithConstraints(Modifier.fillMaxSize()) {
		val screenW = with(density) { maxWidth.toPx() }
		val screenH = with(density) { maxHeight.toPx() }

		val scaledW = baseWidth * scaleAnim
		val scaledH = baseHeight * scaleAnim
		val offsetX = if (anchor == HudAnchor.TOP_LEFT) {
			pos.x * screenW
		} else {
			screenW - pos.x * screenW - scaledW
		}
		val offsetY = pos.y * screenH

		Box(
			modifier = Modifier
				.offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
				.graphicsLayer {
					scaleX = scaleAnim
					scaleY = scaleAnim
					transformOrigin = if (anchor == HudAnchor.TOP_LEFT) {
						TransformOrigin(0f, 0f)
					} else {
						TransformOrigin(1f, 0f)
					}
				}
				.then(
					if (editMode) {
						Modifier.border(if (selected) 2.dp else 1.dp, borderColor, RoundedCornerShape(4.dp))
					} else Modifier
				)
				.pointerInput(interactive, id) {
					if (!interactive) return@pointerInput
					detectTapGestures { HudEditorManager.selectedId = id }
				}
				.pointerInput(interactive, id, screenW, screenH) {
					if (!interactive) return@pointerInput
					detectDragGestures(
						onDragStart = { HudEditorManager.selectedId = id },
						onDrag = { change, dragAmount ->
							change.consume()
							val maxX = (1f - baseWidth * pos.scale / screenW).coerceIn(0f, 1f)
							val maxY = (1f - baseHeight * pos.scale / screenH).coerceIn(0f, 1f)
							val newX = if (anchor == HudAnchor.TOP_LEFT) {
								pos.x + dragAmount.x / screenW
							} else {
								pos.x - dragAmount.x / screenW
							}
							pos.x = newX.coerceIn(0f, maxX)
							pos.y = (pos.y + dragAmount.y / screenH).coerceIn(0f, maxY)
						}
					)
				}
				.pointerInput(interactive, id) {
					if (!interactive) return@pointerInput
					awaitPointerEventScope {
						while (true) {
							val event = awaitPointerEvent()
							if (event.type == PointerEventType.Scroll && HudEditorManager.selectedId == id) {
								val delta = event.changes.firstOrNull()?.scrollDelta?.y ?: 0f
								if (delta != 0f) {
									val target = round((pos.scale - delta * 0.05f) * 20f) / 20f
									pos.scale = target.coerceIn(0.5f, 2f)
								}
							}
						}
					}
				}
				.onGloballyPositioned { coords ->
					baseWidth = coords.size.width.toFloat()
					baseHeight = coords.size.height.toFloat()
				}
		) {
			content()
			if (selected && editMode) {
				Box(
					modifier = Modifier
						.align(Alignment.BottomCenter)
						.graphicsLayer { translationY = 22f }
				) {
					Row(
						modifier = Modifier
							.background(Color.Black.copy(alpha = 0.8f), RoundedCornerShape(4.dp))
							.clip(RoundedCornerShape(4.dp))
							.padding(horizontal = 8.dp, vertical = 2.dp)
					) {
						Text(
							"${(pos.scale * 100).roundToInt()}%",
							color = Color.White,
							fontSize = 10.sp,
							fontWeight = FontWeight.Bold
						)
					}
				}
			}
		}
	}
}
