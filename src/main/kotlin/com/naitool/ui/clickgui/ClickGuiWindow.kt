package com.naitool.ui.clickgui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

/** 窗口位置（dp 坐标 = GUI 逻辑坐标） */
class WindowPos(x: Float, y: Float) {
	var x by mutableStateOf(x)
	var y by mutableStateOf(y)
}

private const val WINDOW_WIDTH = 96
private val titleFont = 9.sp
private val moduleFont = 7.5.sp
private val settingFont = 6.5.sp
private val descFont = 5.5.sp
private val radius = 2.dp

@Composable
fun ClickGuiWindow(
	category: CategoryModel,
	pos: WindowPos,
	onBringToFront: () -> Unit
) {
	val interactionSource = remember { MutableInteractionSource() }
	val hovered by interactionSource.collectIsHoveredAsState()
	val contentScroll = rememberScrollState()

	Column(
		modifier = Modifier
			.offset {
				IntOffset(pos.x.dp.roundToPx(), pos.y.dp.roundToPx())
			}
			.width(WINDOW_WIDTH.dp)
			.heightIn(max = 210.dp)
			.background(ClickGuiColors.backgroundColor, RoundedCornerShape(radius))
	) {
		Card(
			modifier = Modifier
				.fillMaxWidth()
				.height(13.dp)
				.shadow(5.dp, RoundedCornerShape(radius))
				.hoverable(interactionSource)
				.pointerInput(Unit) {
					val density = this.density
					detectDragGestures(
						onDragStart = { onBringToFront() }
					) { _, dragAmount ->
						pos.x += dragAmount.x / density
						pos.y += dragAmount.y / density
					}
				},
			colors = CardDefaults.cardColors(containerColor = ClickGuiColors.titleBgColor),
			elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
			shape = RoundedCornerShape(radius)
		) {
			Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
				Text(
					text = category.title,
					fontSize = titleFont,
					fontWeight = FontWeight.Normal,
					color = ClickGuiColors.textColor,
					textAlign = TextAlign.Center
				)
			}
		}

		Column(
			modifier = Modifier
				.fillMaxWidth()
				.verticalScroll(contentScroll)
		) {
			category.modules.forEach { module ->
				ModuleItem(module)
			}
		}
	}
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ModuleItem(module: ModuleModel) {
	val interactionSource = remember { MutableInteractionSource() }
	val hovered by interactionSource.collectIsHoveredAsState()
	var expanded by remember { mutableStateOf(false) }

	val enabled = module.toggle?.checked == true
	val fontWeight by animateFloatAsState(if (enabled) 600f else 400f)
	val bgColor by animateColorAsState(
		targetValue = when {
			hovered -> ClickGuiColors.moduleHoverBgColor
			enabled -> ClickGuiColors.moduleEnabledBgColor
			else -> ClickGuiColors.titleBgColor
		},
		animationSpec = tween(durationMillis = 200, easing = LinearEasing)
	)
	val arrowRotation by animateFloatAsState(if (expanded) 90f else 0f, tween(durationMillis = 200))

	Column(modifier = Modifier.fillMaxWidth()) {
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.background(bgColor)
				.combinedClickable(
					interactionSource = remember { MutableInteractionSource() },
					indication = null,
					onClick = { module.toggle?.set(!enabled) },
					onLongClick = { expanded = !expanded }
				)
				.hoverable(interactionSource)
				.padding(horizontal = 6.dp, vertical = 2.dp),
			horizontalArrangement = Arrangement.SpaceBetween,
			verticalAlignment = Alignment.CenterVertically
		) {
			Text(
				text = module.title,
				fontSize = moduleFont,
				fontWeight = FontWeight(fontWeight.toInt()),
				color = ClickGuiColors.textColor,
				modifier = Modifier.weight(1f)
			)
			Text(
				text = "▶",
				fontSize = 6.sp,
				color = ClickGuiColors.textColor.copy(alpha = 0.7f),
				modifier = Modifier
					.rotate(arrowRotation)
					.clickable { expanded = !expanded }
			)
		}

		AnimatedVisibility(
			visible = expanded,
			enter = expandVertically(),
			exit = shrinkVertically()
		) {
			Column(
				modifier = Modifier
					.fillMaxWidth()
					.background(ClickGuiColors.settingBgColor)
					.padding(start = 6.dp, end = 4.dp, top = 2.dp, bottom = 2.dp)
					.animateContentSize()
			) {
				module.settings.forEach { setting ->
					SettingItem(setting)
				}
				module.keybind?.let { keybind ->
					KeybindItem(keybind)
				}
			}
		}
	}
}

@Composable
private fun SettingItem(setting: SettingModel) {
	when (setting) {
		is BooleanModel -> BooleanSettingItem(setting)
		is NumberModel -> NumberSettingItem(setting)
		is ModeModel -> ModeSettingItem(setting)
		is ColorModel -> ColorSettingItem(setting)
		is StringModel -> StringSettingItem(setting)
	}
}

@Composable
private fun SettingLabel(name: String, description: String) {
	Column {
		Text(text = name, fontSize = settingFont, color = ClickGuiColors.textSecondaryColor)
		if (description.isNotEmpty()) {
			Text(
				text = description,
				fontSize = descFont,
				color = ClickGuiColors.textSecondaryColor.copy(alpha = 0.5f),
				maxLines = 1,
				overflow = TextOverflow.Ellipsis
			)
		}
	}
}

@Composable
private fun BooleanSettingItem(setting: BooleanModel) {
	val interactionSource = remember { MutableInteractionSource() }
	val hovered by interactionSource.collectIsHoveredAsState()
	val bgAlpha by animateFloatAsState(if (hovered) 1f else 0f, tween(150))

	Row(
		modifier = Modifier
			.fillMaxWidth()
			.background(ClickGuiColors.settingHoverColor.copy(alpha = bgAlpha))
			.hoverable(interactionSource)
			.clickable { setting.set(!setting.checked) }
			.padding(vertical = 1.dp, horizontal = 3.dp),
		horizontalArrangement = Arrangement.SpaceBetween,
		verticalAlignment = Alignment.CenterVertically
	) {
		Box(Modifier.weight(1f).padding(end = 4.dp)) {
			SettingLabel(setting.displayName, setting.description)
		}
		CustomSwitch(
			checked = setting.checked,
			onCheckedChange = { setting.set(it) }
		)
	}
}

@Composable
private fun NumberSettingItem(setting: NumberModel) {
	val interactionSource = remember { MutableInteractionSource() }
	val hovered by interactionSource.collectIsHoveredAsState()
	val bgAlpha by animateFloatAsState(if (hovered) 1f else 0f, tween(150))

	Column(
		modifier = Modifier
			.fillMaxWidth()
			.background(ClickGuiColors.settingHoverColor.copy(alpha = bgAlpha))
			.hoverable(interactionSource)
			.padding(vertical = 1.dp, horizontal = 3.dp)
	) {
		Row(
			modifier = Modifier.fillMaxWidth(),
			horizontalArrangement = Arrangement.SpaceBetween,
			verticalAlignment = Alignment.CenterVertically
		) {
			Box(Modifier.weight(1f).padding(end = 4.dp)) {
				SettingLabel(setting.displayName, setting.description)
			}
			Text(
				text = setting.formatValue(),
				fontSize = settingFont,
				color = ClickGuiColors.accentColor
			)
		}
		CustomSlider(
			value = setting.value,
			onValueChange = { setting.set(it, commit = false) },
			onValueChangeFinished = { setting.set(setting.value, commit = true) },
			valueRange = setting.min..setting.max,
			modifier = Modifier.fillMaxWidth().height(10.dp),
			trackHeight = 2.dp,
			thumbSize = 6.dp
		)
	}
}

@Composable
private fun ModeSettingItem(setting: ModeModel) {
	var expanded by remember { mutableStateOf(false) }
	val interactionSource = remember { MutableInteractionSource() }
	val hovered by interactionSource.collectIsHoveredAsState()
	val bgAlpha by animateFloatAsState(if (hovered || expanded) 1f else 0f, tween(150))

	Column(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp, horizontal = 4.dp)) {
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.background(ClickGuiColors.settingHoverColor.copy(alpha = bgAlpha))
				.hoverable(interactionSource)
				.clickable { expanded = !expanded }
				.padding(vertical = 2.dp),
			horizontalArrangement = Arrangement.SpaceBetween,
				verticalAlignment = Alignment.CenterVertically
			) {
				Box(Modifier.weight(1f).padding(end = 4.dp)) {
					SettingLabel(setting.displayName, setting.description)
				}
				Row(verticalAlignment = Alignment.CenterVertically) {
					Text(
						text = setting.selected.displayName,
						fontSize = settingFont,
						color = ClickGuiColors.accentColor
					)
				Text(
					text = if (expanded) " ▲" else " ▼",
					fontSize = 6.sp,
					color = ClickGuiColors.textColor.copy(alpha = 0.6f)
				)
			}
		}

		AnimatedVisibility(visible = expanded, enter = expandVertically(), exit = shrinkVertically()) {
			Column(
				modifier = Modifier
					.fillMaxWidth()
					.background(ClickGuiColors.dropdownBgColor)
					.padding(2.dp)
			) {
				setting.entries.forEach { entry ->
					val modeInteraction = remember { MutableInteractionSource() }
					val modeHovered by modeInteraction.collectIsHoveredAsState()
					val selected = entry == setting.selected
					val alpha by animateFloatAsState(
						targetValue = when {
							selected -> 0.6f
							modeHovered -> 1f
							else -> 0f
						},
						animationSpec = tween(150)
					)
					Row(
						modifier = Modifier
							.fillMaxWidth()
							.background(
								if (selected) ClickGuiColors.accentColor
								else ClickGuiColors.settingHoverColor
								.copy(alpha = alpha)
							)
							.hoverable(modeInteraction)
							.clickable {
								setting.select(entry)
								expanded = false
							}
							.padding(horizontal = 6.dp, vertical = 3.dp),
						verticalAlignment = Alignment.CenterVertically
					) {
						Text(
							text = entry.displayName,
							fontSize = settingFont,
							color = if (selected) ClickGuiColors.textColor
							else ClickGuiColors.textColor.copy(alpha = 0.8f)
						)
					}
				}
			}
		}
	}
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun ColorSettingItem(setting: ColorModel) {
	var expanded by remember { mutableStateOf(false) }
	val interactionSource = remember { MutableInteractionSource() }
	val hovered by interactionSource.collectIsHoveredAsState()
	val bgAlpha by animateFloatAsState(if (hovered || expanded) 1f else 0f, tween(150))

	var red by remember { mutableStateOf((setting.rgb shr 16 and 0xFF).toFloat()) }
	var green by remember { mutableStateOf((setting.rgb shr 8 and 0xFF).toFloat()) }
	var blue by remember { mutableStateOf((setting.rgb and 0xFF).toFloat()) }

	fun apply() {
		setting.set(((red.toInt() and 0xFF) shl 16) or ((green.toInt() and 0xFF) shl 8) or (blue.toInt() and 0xFF))
	}

	Column(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp, horizontal = 4.dp)) {
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.background(ClickGuiColors.settingHoverColor.copy(alpha = bgAlpha))
				.hoverable(interactionSource)
				.clickable { expanded = !expanded }
				.padding(vertical = 2.dp),
			horizontalArrangement = Arrangement.SpaceBetween,
			verticalAlignment = Alignment.CenterVertically
		) {
			Box(Modifier.weight(1f).padding(end = 4.dp)) {
				SettingLabel(setting.displayName, setting.description)
			}
			Row(verticalAlignment = Alignment.CenterVertically) {
				Box(
					modifier = Modifier
						.size(14.dp, 9.dp)
						.background(
							Color(red / 255f, green / 255f, blue / 255f),
							RoundedCornerShape(2.dp)
						)
						.border(1.dp, ClickGuiColors.textColor.copy(alpha = 0.5f), RoundedCornerShape(2.dp))
				)
				Text(
					text = if (expanded) " ▲" else " ▼",
					fontSize = 6.sp,
					color = ClickGuiColors.textColor.copy(alpha = 0.6f)
				)
			}
		}

		AnimatedVisibility(visible = expanded, enter = expandVertically(), exit = shrinkVertically()) {
			Column(
				modifier = Modifier
					.fillMaxWidth()
					.background(ClickGuiColors.dropdownBgColor)
					.padding(4.dp)
			) {
				Box(
					modifier = Modifier
						.fillMaxWidth()
						.height(22.dp)
						.clip(RoundedCornerShape(4.dp))
						.background(
							Brush.horizontalGradient(
								listOf(
									Color.Red, Color.Yellow, Color.Green,
									Color.Cyan, Color.Blue, Color.Magenta, Color.Red
								)
							)
						)
						.pointerInput(Unit) {
							detectTapGestures { offset ->
								val hue = (offset.x / size.width) * 360f
								val (r, g, b) = hueToRgb(hue)
								red = r; green = g; blue = b
								apply(); setting.commit()
							}
						}
						.pointerInput(Unit) {
							detectHorizontalDragGestures { change, _ ->
								change.consume()
								val hue = (change.position.x.coerceIn(0f, size.width.toFloat()) / size.width) * 360f
								val (r, g, b) = hueToRgb(hue)
								red = r; green = g; blue = b
								apply()
							}
						}
				)

				Spacer(modifier = Modifier.height(4.dp))
				ColorSliderRow("R", red, Color.Red) { red = it; apply() }
				ColorSliderRow("G", green, Color.Green) { green = it; apply() }
				ColorSliderRow("B", blue, Color.Blue) { blue = it; apply() }
			}
		}
	}
}

@Composable
private fun ColorSliderRow(label: String, value: Float, color: Color, onValueChange: (Float) -> Unit) {
	Row(
		modifier = Modifier.fillMaxWidth().padding(vertical = 1.dp),
		verticalAlignment = Alignment.CenterVertically
	) {
		Text(text = label, fontSize = 6.sp, color = color, modifier = Modifier.width(10.dp))
		CustomSlider(
			value = value,
			onValueChange = onValueChange,
			onValueChangeFinished = { ConfigBridge.save() },
			valueRange = 0f..255f,
			modifier = Modifier.weight(1f).height(10.dp),
			trackHeight = 2.dp,
			thumbSize = 6.dp,
			activeColor = color
		)
		Text(
			text = value.toInt().toString(),
			fontSize = 6.sp,
			color = ClickGuiColors.textColor,
			modifier = Modifier.width(22.dp),
			textAlign = TextAlign.End
		)
	}
}

@Composable
private fun KeybindItem(model: KeybindModel) {
	val interactionSource = remember { MutableInteractionSource() }
	val hovered by interactionSource.collectIsHoveredAsState()
	val listening = KeybindListener.target === model
	val bgAlpha by animateFloatAsState(if (hovered || listening) 1f else 0f, tween(150))

	Row(
		modifier = Modifier
			.fillMaxWidth()
			.background(ClickGuiColors.settingHoverColor.copy(alpha = bgAlpha))
			.hoverable(interactionSource)
			.clickable {
				KeybindListener.target = if (listening) null else model
			}
			.padding(vertical = 1.dp, horizontal = 3.dp),
		horizontalArrangement = Arrangement.SpaceBetween,
		verticalAlignment = Alignment.CenterVertically
	) {
		Box(Modifier.weight(1f).padding(end = 4.dp)) {
			SettingLabel(model.displayName, model.description)
		}
		Box(
			modifier = Modifier
				.background(ClickGuiColors.dropdownBgColor, RoundedCornerShape(2.dp))
				.border(
					1.dp,
					if (listening) Color.Yellow else ClickGuiColors.accentColor.copy(alpha = 0.7f),
					RoundedCornerShape(2.dp)
				)
				.padding(horizontal = 5.dp, vertical = 2.dp)
		) {
			Text(
				text = if (listening) {
					fi.dy.masa.malilib.util.StringUtils.translate("naitool.ui.clickgui.listening")
				} else {
					model.keyDisplay()
				},
				fontSize = settingFont,
				color = if (listening) Color.Yellow else ClickGuiColors.accentColor
			)
		}
	}
}

@Composable
private fun StringSettingItem(setting: StringModel) {
	val interactionSource = remember { MutableInteractionSource() }
	val hovered by interactionSource.collectIsHoveredAsState()
	val bgAlpha by animateFloatAsState(if (hovered) 1f else 0f, tween(150))
	var text by remember(setting) { mutableStateOf(setting.text) }

	Column(
		modifier = Modifier
			.fillMaxWidth()
			.background(ClickGuiColors.settingHoverColor.copy(alpha = bgAlpha))
			.hoverable(interactionSource)
			.padding(vertical = 1.dp, horizontal = 3.dp)
	) {
		Box(Modifier.fillMaxWidth().padding(bottom = 2.dp)) {
			SettingLabel(setting.displayName, setting.description)
		}
		BasicTextField(
			value = text,
			onValueChange = {
				if (it.length <= 64) {
					text = it
					setting.set(it)
				}
			},
			singleLine = true,
			textStyle = TextStyle(
				fontSize = settingFont,
				color = ClickGuiColors.textColor
			),
			cursorBrush = SolidColor(ClickGuiColors.accentColor),
			keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
			keyboardActions = KeyboardActions(onDone = { setting.commit() }),
			modifier = Modifier
				.fillMaxWidth()
				.background(ClickGuiColors.dropdownBgColor, RoundedCornerShape(2.dp))
				.border(
					1.dp,
					ClickGuiColors.textColor.copy(alpha = 0.25f),
					RoundedCornerShape(2.dp)
				)
				.padding(horizontal = 5.dp, vertical = 3.dp)
				.onFocusChanged { focusState ->
					if (!focusState.hasFocus) setting.commit()
				}
		)
	}
}
