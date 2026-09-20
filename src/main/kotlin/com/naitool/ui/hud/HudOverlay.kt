package com.naitool.ui.hud

import androidx.compose.ui.InternalComposeUiApi
import androidx.compose.ui.graphics.asComposeCanvas
import androidx.compose.ui.scene.CanvasLayersComposeScene
import androidx.compose.ui.scene.ComposeScene
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntSize
import net.minecraft.client.Minecraft
import org.jetbrains.skia.Canvas

/**
 * 游戏内常驻 HUD 的独立 ComposeScene（不属于任何 Screen）。
 * 由 ComposeRenderer 在没有界面（或 ClickGui 非编辑态）时每帧渲染。
 * 场景坐标系/密度与 ComposeScreen 保持一致（物理像素）。
 */
@OptIn(InternalComposeUiApi::class)
object HudOverlay {
	private var scene: ComposeScene? = null
	private var curWidth = 0
	private var curHeight = 0
	private var curDensity = -1f

	private fun density(): Float {
		val w = Minecraft.getInstance().window
		val scaledWidth = w.guiScaledWidth
		return if (scaledWidth > 0) w.width.toFloat() / scaledWidth else w.guiScale.toFloat()
	}

	fun ensureAndRender(canvas: Canvas, frameTimeNanos: Long, width: Int, height: Int) {
		val d = density()
		var s = scene
		if (s == null) {
			s = CanvasLayersComposeScene(density = Density(d), invalidate = {}).apply {
				size = IntSize(width, height)
				setContent { HudRoot(interactive = false) }
			}
			scene = s
			curWidth = width
			curHeight = height
			curDensity = d
		} else {
			if (curDensity != d) {
				s.density = Density(d)
				curDensity = d
			}
			if (curWidth != width || curHeight != height) {
				s.size = IntSize(width, height)
				curWidth = width
				curHeight = height
			}
		}
		s.render(canvas.asComposeCanvas(), frameTimeNanos)
	}

	fun close() {
		scene?.close()
		scene = null
		curWidth = 0
		curHeight = 0
	}
}
