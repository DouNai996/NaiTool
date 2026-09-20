package com.naitool.ui.core

import com.naitool.ui.clickgui.ClickGuiScreen
import com.naitool.ui.hud.HudEditorManager
import com.naitool.ui.hud.HudOverlay
import com.naitool.ui.hud.HudState
import net.minecraft.client.Minecraft
import org.jetbrains.skia.BackendRenderTarget
import org.jetbrains.skia.Canvas
import org.jetbrains.skia.ColorSpace
import org.jetbrains.skia.DirectContext
import org.jetbrains.skia.FramebufferFormat
import org.jetbrains.skia.Surface
import org.jetbrains.skia.SurfaceColorFormat
import org.jetbrains.skia.SurfaceOrigin
import org.lwjgl.opengl.GL33C
import org.slf4j.LoggerFactory

/**
 * 全局 Skia/Compose 帧渲染器。
 *
 * 调用时机（见 ComposeFrameMixin）：Minecraft#renderFrame 中
 * mainRenderTarget.blitToScreen() 之后、RenderSystem.flipFrame()（交换缓冲区）之前，
 * 渲染线程、默认帧缓冲已绑定。
 *
 * 每帧顺序渲染：HUD 层（M4）→ 当前 ComposeScreen 层，多个 ComposeScene 共用
 * 同一个 Skia Surface（同一块帧缓冲画布，顺序叠加）。
 */
object ComposeRenderer {
	private val LOGGER = LoggerFactory.getLogger("NaiTool/Compose")

	private var skiaContext: DirectContext? = null
	private var renderTarget: BackendRenderTarget? = null
	private var surface: Surface? = null
	private var boundFbo: Int = -1
	private var surfaceWidth: Int = 0
	private var surfaceHeight: Int = 0
	private var failed: Boolean = false

	@JvmStatic
	var screenLayer: ComposeScreen? = null

	@JvmStatic
	fun onFrameRendered() {
		if (failed) return
		val layer = screenLayer
		try {
			val window = Minecraft.getInstance().window
			val width = window.width
			val height = window.height
			if (width <= 0 || height <= 0) return

			val showHud = shouldRenderHud(layer)
			if (layer == null && !showHud) return

			val fbo = GL33C.glGetInteger(GL33C.GL_DRAW_FRAMEBUFFER_BINDING)
			if (surface == null || surfaceWidth != width || surfaceHeight != height || fbo != boundFbo) {
				rebuild(width, height, fbo)
			}

			val activeSurface = surface ?: return
			val ctx = skiaContext ?: return

			GlStateUtil.save()
			try {
				resetPixelStore()
				ctx.resetAll()
				GL33C.glEnable(GL33C.GL_BLEND)
				val canvas = activeSurface.canvas
				val now = System.nanoTime()
				if (showHud) {
					HudOverlay.ensureAndRender(canvas, now, width, height)
				}
				layer?.renderScene(canvas, now)
				activeSurface.flushAndSubmit()
			} finally {
				GlStateUtil.restore()
			}
		} catch (t: Throwable) {
			LOGGER.error("[NaiTool] Compose overlay render failed", t)
		}
	}

	/**
	 * HUD 显示条件：总开关开启，且
	 * - 没有打开任何 Screen（游戏内），或
	 * - 打开的是 ClickGui 且当前不在 HUD 编辑态（编辑态的交互式 HUD 由 ClickGui 场景自身渲染）
	 */
	private fun shouldRenderHud(layer: ComposeScreen?): Boolean {
		if (!HudState.hudEnabled()) return false
		if (HudEditorManager.editMode) return false
		val mcScreen = Minecraft.getInstance().screen
		return mcScreen == null || layer is ClickGuiScreen
	}

	private fun resetPixelStore() {
		GL33C.glBindBuffer(GL33C.GL_PIXEL_UNPACK_BUFFER, 0)
		GL33C.glPixelStorei(GL33C.GL_UNPACK_SWAP_BYTES, GL33C.GL_FALSE)
		GL33C.glPixelStorei(GL33C.GL_UNPACK_LSB_FIRST, GL33C.GL_FALSE)
		GL33C.glPixelStorei(GL33C.GL_UNPACK_ROW_LENGTH, 0)
		GL33C.glPixelStorei(GL33C.GL_UNPACK_SKIP_ROWS, 0)
		GL33C.glPixelStorei(GL33C.GL_UNPACK_SKIP_PIXELS, 0)
		GL33C.glPixelStorei(GL33C.GL_UNPACK_ALIGNMENT, 4)
	}

	private fun rebuild(width: Int, height: Int, fbo: Int) {
		surface?.close()
		renderTarget?.close()
		skiaContext?.close()

		val ctx = DirectContext.makeGL()
		val rt = BackendRenderTarget.makeGL(
			width, height, 0, 8, fbo,
			FramebufferFormat.GR_GL_RGBA8
		)
		val newSurface = Surface.makeFromBackendRenderTarget(
			ctx, rt,
			SurfaceOrigin.BOTTOM_LEFT,
			SurfaceColorFormat.BGRA_8888,
			ColorSpace.sRGB
		)
		skiaContext = ctx
		renderTarget = rt
		surface = newSurface
		boundFbo = fbo
		surfaceWidth = width
		surfaceHeight = height
		LOGGER.info("[NaiTool] Skia surface ready: {}x{} fbo={}", width, height, fbo)
	}
}
