package com.naitool.ui.clickgui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.naitool.ui.core.ComposeScreen
import com.naitool.ui.hud.HudEditorManager
import com.naitool.ui.hud.HudRoot
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.input.KeyEvent as McKeyEvent
import net.minecraft.network.chat.Component
import fi.dy.masa.malilib.util.StringUtils
import org.lwjgl.glfw.GLFW

private data class WindowEntry(val category: CategoryModel, val pos: WindowPos)

/**
 * @param parentScreen 打开来源（如自定义标题屏、ModMenu）。关闭时返回该屏幕；
 *                     游戏内热键打开时为 null，关闭回到游戏。
 */
class ClickGuiScreen @JvmOverloads constructor(
	private val parentScreen: Screen? = null
) : ComposeScreen(Component.literal("NaiTool ClickGui")) {

	private val categories: List<CategoryModel> = ConfigBridge.build()
	private val windowOrder = mutableStateListOf<CategoryModel>()

	@Composable
	override fun renderCompose() {
		var appeared by remember { mutableStateOf(false) }
		LaunchedEffect(Unit) { appeared = true }
		val dimAlpha by animateFloatAsState(if (appeared) 0.5f else 0f, tween(200))

		val entries = remember {
			// 所有分类固定排在顶部一排，避免换行重叠；窄屏下按可用宽度等比收缩间距
			val window = Minecraft.getInstance().window
			val screenWidth = window.guiScaledWidth
			val widthDp = 96
			val minSpacing = 4
			val totalWidth = categories.size * widthDp + (categories.size + 1) * minSpacing
			val spacing = if (totalWidth <= screenWidth) {
				minSpacing
			} else {
				((screenWidth - categories.size * widthDp) / (categories.size + 1)).coerceAtLeast(0)
			}

			categories.mapIndexed { index, category ->
				val x = (spacing + index * (widthDp + spacing)).toFloat()
				val y = 6f
				WindowEntry(category, WindowPos(x, y))
			}
		}

		if (windowOrder.isEmpty()) {
			windowOrder.addAll(categories)
		}

		Box(modifier = Modifier.fillMaxSize()) {
			Box(
				modifier = Modifier
					.fillMaxSize()
					.graphicsLayer(alpha = dimAlpha)
					.background(androidx.compose.ui.graphics.Color.Black)
			)

			if (HudEditorManager.editMode) {
				// 编辑态：交互式 HUD（拖拽/滚轮缩放由 HudComponent 处理）
				HudRoot(interactive = true)
			} else {
				windowOrder.forEach { category ->
					val entry = entries.first { it.category === category }
					ClickGuiWindow(
						category = entry.category,
						pos = entry.pos,
						onBringToFront = {
							windowOrder.remove(category)
							windowOrder.add(category)
						}
					)
				}
			}

			Button(
				onClick = { HudEditorManager.toggleEditMode() },
				contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp),
				modifier = Modifier
					.align(Alignment.BottomEnd)
					.padding(8.dp),
				colors = ButtonDefaults.buttonColors(
					containerColor = if (HudEditorManager.editMode) {
						androidx.compose.ui.graphics.Color(108, 53, 222)
					} else {
						androidx.compose.ui.graphics.Color(40, 40, 40)
					},
					contentColor = androidx.compose.ui.graphics.Color.White
				),
				shape = RoundedCornerShape(6.dp)
			) {
				Text(
					text = StringUtils.translate(
						if (HudEditorManager.editMode) {
							"naitool.ui.clickgui.finish_edit"
						} else {
							"naitool.ui.clickgui.edit_hud"
						}
					),
					fontSize = 7.sp
				)
			}
		}
	}

	override fun keyPressed(event: McKeyEvent): Boolean {
		val listening = KeybindListener.target
		if (listening != null) {
			if (event.key() == GLFW.GLFW_KEY_ESCAPE) {
				listening.unbind()
			} else {
				listening.bind(event.key())
			}
			KeybindListener.target = null
			return true
		}
		if (event.key() == GLFW.GLFW_KEY_ESCAPE) {
			if (HudEditorManager.editMode) {
				HudEditorManager.exitEditMode()
			} else {
				onClose()
			}
			return true
		}
		return super.keyPressed(event)
	}

	override fun onClose() {
		// 有来源屏幕时返回来源（如自定义标题屏），避免 setScreen(null) 在未进世界时
		// 触发 Minecraft 内部 new TitleScreen()（该路径不经过 setScreen，替换 Mixin 拦不到）
		Minecraft.getInstance().setScreen(parentScreen)
	}

	override fun removed() {
		KeybindListener.target = null
		if (HudEditorManager.editMode) HudEditorManager.exitEditMode()
		ConfigBridge.save()
		super.removed()
	}
}
