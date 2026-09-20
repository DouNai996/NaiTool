package com.naitool.ui.hud

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fi.dy.masa.malilib.util.StringUtils
import kotlinx.coroutines.delay

/**
 * 常驻 HUD 根节点。
 * - interactive=false：游戏内由 HudOverlay 的独立 ComposeScene 渲染（无输入）
 * - interactive=true：ClickGui 的 HUD 编辑模式中由 ClickGuiScreen 场景渲染（可拖拽/缩放）
 */
@Composable
fun HudRoot(interactive: Boolean) {
	val editMode = HudEditorManager.editMode

	var features by remember { mutableStateOf(emptyList<HudFeature>()) }
	LaunchedEffect(Unit) {
		while (true) {
			features = HudState.enabledFeatures()
			NotificationManager.prune(System.currentTimeMillis())
			delay(150)
		}
	}

	val infiniteTransition = rememberInfiniteTransition(label = "hudRainbow")
	val hueOffset by infiniteTransition.animateFloat(
		initialValue = 0f,
		targetValue = 1f,
		animationSpec = infiniteRepeatable(tween(1000, easing = LinearEasing), RepeatMode.Restart),
		label = "hudHue"
	)
	var timeMs by remember { mutableStateOf(0L) }
	LaunchedEffect(Unit) {
		while (true) {
			timeMs = System.currentTimeMillis()
			delay(16)
		}
	}

	Box(Modifier.fillMaxSize()) {
		// 左上角水印
		if (HudState.watermarkEnabled() || editMode) {
			HudComponent(
				id = "watermark",
				anchor = HudAnchor.TOP_LEFT,
				interactive = interactive
			) {
				Column(modifier = Modifier.padding(4.dp)) {
					Text(
						text = "NaiTool",
						fontSize = 20.sp,
						fontWeight = FontWeight.Bold,
						color = Color.White,
						style = TextStyle(shadow = Shadow(Color.Black, Offset(1f, 1f), 5f))
					)
					Text(
						text = "26.1.2",
						fontSize = 7.sp,
						color = Color(150, 120, 255),
						style = TextStyle(shadow = Shadow(Color.Black, Offset(1f, 1f), 4f))
					)
				}
			}
		}

		// 右上角功能 ArrayList（按文字宽度降序）
		val displayFeatures = if (features.isEmpty() && editMode) {
			listOf(
				HudFeature(StringUtils.translate("naitool.ui.clickgui.category.boost"), null),
				HudFeature(StringUtils.translate("naitool.ui.clickgui.category.trails"), "| ${StringUtils.translate("naitool.config.generic.elytraTrailsMode.rainbow")}")
			)
		} else features

		if (displayFeatures.isNotEmpty()) {
			HudComponent(
				id = "arrayList",
				anchor = HudAnchor.TOP_RIGHT,
				interactive = interactive
			) {
				val fontSize = HudState.fontSize().sp
				val measurer = rememberTextMeasurer()
				val sorted = remember(displayFeatures, fontSize.value) {
					displayFeatures.sortedByDescending { f ->
						measurer.measure(
							f.fullText,
							style = TextStyle(fontSize = fontSize, textAlign = TextAlign.Right)
						).size.width
					}
				}
				Column(horizontalAlignment = Alignment.End) {
					sorted.forEachIndexed { index, feature ->
						val rowColor = HudState.rowColor(index, sorted.size, hueOffset, timeMs)
						val bg = if (HudState.backgroundEnabled()) {
							Modifier.background(Color(0f, 0f, 0f, 0.55f), RoundedCornerShape(2.dp))
						} else Modifier
						Box(
							modifier = Modifier
								.padding(vertical = 1.dp)
								.then(bg)
								.padding(horizontal = 4.dp, vertical = 1.dp)
						) {
							Text(
								text = feature.fullText,
								fontSize = fontSize,
								color = rowColor,
								textAlign = TextAlign.Right,
								style = TextStyle(shadow = Shadow(Color.Black, Offset(1f, 1f), 4f))
							)
						}
					}
				}
			}
		}

		// 通知（右下角）
		Column(
			modifier = Modifier
				.fillMaxSize()
				.padding(bottom = 16.dp, end = 12.dp),
			verticalArrangement = androidx.compose.foundation.layout.Arrangement.Bottom,
			horizontalAlignment = Alignment.End
		) {
			NotificationManager.items.forEach { n ->
				NotificationCard(n)
			}
		}

		if (interactive && editMode) {
			Box(
				modifier = Modifier
					.fillMaxSize()
					.padding(bottom = 52.dp),
				contentAlignment = Alignment.BottomCenter
			) {
				Text(
					text = StringUtils.translate("naitool.ui.hud.edit_hint"),
					fontSize = 9.sp,
					color = Color.White,
					modifier = Modifier
						.background(Color(0f, 0f, 0f, 0.6f), RoundedCornerShape(4.dp))
						.padding(horizontal = 8.dp, vertical = 4.dp)
				)
			}
		}
	}
}

@Composable
private fun NotificationCard(n: Notification) {
	var visible by remember(n.id) { mutableStateOf(false) }
	LaunchedEffect(n.id) {
		visible = true
		delay((n.ttlMs - 220).coerceAtLeast(50))
		visible = false
		delay(240)
		NotificationManager.items.remove(n)
	}
	AnimatedVisibility(
		visible = visible,
		enter = slideInHorizontally { it / 2 } + fadeIn(),
		exit = slideOutHorizontally { it / 2 } + fadeOut()
	) {
		Box(
			modifier = Modifier
				.padding(vertical = 2.dp)
				.widthIn(min = 90.dp)
				.background(Color(26, 26, 26, 230), RoundedCornerShape(6.dp))
				.padding(horizontal = 10.dp, vertical = 5.dp)
		) {
			if (n.detail != null) {
				// 自定义文案通知：标题（次要色） + 自定义内容（金色）
				Text(
					text = buildAnnotatedString {
						withStyle(SpanStyle(color = Color(0xBB, 0xBB, 0xBB))) {
							append(n.title)
							append("  ")
						}
						withStyle(SpanStyle(color = Color(0xFF, 0xB3, 0x47))) {
							append(n.detail)
						}
					},
					fontSize = 9.sp,
					style = TextStyle(shadow = Shadow(Color.Black, Offset(1f, 1f), 3f))
				)
			} else {
				val actionColor = if (n.enabled) Color(0x6C, 0xD8, 0x6C) else Color(0xFF, 0x6B, 0x6B)
				val actionText = if (n.enabled) {
					StringUtils.translate("naitool.ui.notify.enabled")
				} else {
					StringUtils.translate("naitool.ui.notify.disabled")
				}
				Text(
					text = buildAnnotatedString {
						withStyle(SpanStyle(color = Color.White)) {
							append(n.title)
							append("  ")
						}
						withStyle(SpanStyle(color = actionColor)) {
							append(actionText)
						}
					},
					fontSize = 9.sp,
					style = TextStyle(shadow = Shadow(Color.Black, Offset(1f, 1f), 3f))
				)
			}
		}
	}
}
