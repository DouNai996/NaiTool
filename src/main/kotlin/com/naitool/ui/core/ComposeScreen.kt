package com.naitool.ui.core

import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.InternalComposeUiApi
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asComposeCanvas
import androidx.compose.ui.input.pointer.PointerButton
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.PointerId
import androidx.compose.ui.input.pointer.PointerType
import androidx.compose.ui.scene.CanvasLayersComposeScene
import androidx.compose.ui.scene.ComposeScene
import androidx.compose.ui.scene.ComposeScenePointer
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntSize
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.input.CharacterEvent
import net.minecraft.client.input.KeyEvent as McKeyEvent
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.network.chat.Component
import org.jetbrains.skia.Canvas
import org.lwjgl.glfw.GLFW
import java.awt.event.KeyEvent as AwtKeyEvent
import java.awt.event.MouseEvent as AwtMouseEvent

/**
 * 基于 Compose Multiplatform 自绘场景的 Screen 基类（26.1.2 Mojmap 版）。
 *
 * - 不使用保留式 GUI 提取管线：extractRenderState 留空，画面全部由
 *   ComposeRenderer 在 flipFrame 前通过 Skia 画到默认帧缓冲。
 * - 鼠标/键盘的 26.1 record 事件包装成 AWT 事件后转发给 ComposeScene。
 * - 场景坐标系为物理像素；density = 物理宽 / GUI 逻辑宽（精确的 GUI 缩放系数）。
 */
@OptIn(InternalComposeUiApi::class)
abstract class ComposeScreen(title: Component) : Screen(title) {

	protected var composeScene: ComposeScene? = null
		private set

	private var lastScale = -1f

	/** GUI 逻辑坐标 → 物理像素 */
	private val scaleFactor: Float
		get() {
			val w = Minecraft.getInstance().window
			val scaledWidth = w.guiScaledWidth
			return if (scaledWidth > 0) w.width.toFloat() / scaledWidth else w.guiScale.toFloat()
		}

	override protected fun init() {
		syncScene()
		ComposeRenderer.screenLayer = this
	}

	override fun resize(width: Int, height: Int) {
		super.resize(width, height)
		syncScene()
	}

	private fun syncScene() {
		val window = Minecraft.getInstance().window
		val density = scaleFactor
		val scene = composeScene
		if (scene == null) {
			composeScene = CanvasLayersComposeScene(
				density = Density(density),
				invalidate = {}
			).apply {
				setContent { renderCompose() }
			}
		} else if (lastScale != density) {
			scene.density = Density(density)
		}
		composeScene?.size = IntSize(window.width, window.height)
		lastScale = density
	}

	fun renderScene(canvas: Canvas, frameTimeNanos: Long) {
		syncScene()
		composeScene?.render(canvas.asComposeCanvas(), frameTimeNanos)
	}

	/** 子类提供 Compose 内容 */
	@Composable
	protected abstract fun renderCompose()

	// 保留式提取管线：不参与（空实现，避免原版背景/控件闪烁）
	override fun extractRenderState(
		extractor: GuiGraphicsExtractor,
		mouseX: Int,
		mouseY: Int,
		partialTick: Float
	) {
	}

	override fun isPauseScreen(): Boolean = false

	override fun shouldCloseOnEsc(): Boolean = false

	private fun toPhysical(value: Double): Float = (value * scaleFactor).toFloat()

	private fun awtMods(): Int = AwtUtils.getAwtMods(Minecraft.getInstance().window.handle())

	@OptIn(ExperimentalComposeUiApi::class, InternalComposeUiApi::class)
	override fun mouseMoved(mouseX: Double, mouseY: Double) {
		val event = AwtUtils.createMouseEvent(
			toPhysical(mouseX).toInt(), toPhysical(mouseY).toInt(),
			awtMods(), 0, AwtMouseEvent.MOUSE_MOVED
		)
		composeScene?.sendPointerEvent(
			PointerEventType.Move,
			position = Offset(toPhysical(mouseX), toPhysical(mouseY)),
			type = PointerType.Mouse,
			button = PointerButton(0),
			nativeEvent = event
		)
	}

	@OptIn(ExperimentalComposeUiApi::class, InternalComposeUiApi::class)
	override fun mouseClicked(event: MouseButtonEvent, doubleClick: Boolean): Boolean {
		val awtEvent = AwtUtils.createMouseEvent(
			toPhysical(event.x()).toInt(), toPhysical(event.y()).toInt(),
			awtMods(), event.button(), AwtMouseEvent.MOUSE_PRESSED
		)
		composeScene?.sendPointerEvent(
			PointerEventType.Press,
			Offset(toPhysical(event.x()), toPhysical(event.y())),
			nativeEvent = awtEvent
		)
		return true
	}

	@OptIn(ExperimentalComposeUiApi::class, InternalComposeUiApi::class)
	override fun mouseReleased(event: MouseButtonEvent): Boolean {
		val awtEvent = AwtUtils.createMouseEvent(
			toPhysical(event.x()).toInt(), toPhysical(event.y()).toInt(),
			awtMods(), event.button(), AwtMouseEvent.MOUSE_RELEASED
		)
		composeScene?.sendPointerEvent(
			PointerEventType.Release,
			Offset(toPhysical(event.x()), toPhysical(event.y())),
			nativeEvent = awtEvent
		)
		return true
	}

	@OptIn(ExperimentalComposeUiApi::class, InternalComposeUiApi::class)
	override fun mouseDragged(event: MouseButtonEvent, dragX: Double, dragY: Double): Boolean {
		val awtEvent = AwtUtils.createMouseEvent(
			toPhysical(event.x()).toInt(), toPhysical(event.y()).toInt(),
			awtMods(), event.button(), AwtMouseEvent.MOUSE_DRAGGED
		)
		val pointer = ComposeScenePointer(
			id = PointerId(0),
			position = Offset(toPhysical(event.x()), toPhysical(event.y())),
			pressed = true,
			type = PointerType.Mouse
		)
		composeScene?.sendPointerEvent(
			eventType = PointerEventType.Move,
			pointers = listOf(pointer),
			nativeEvent = awtEvent
		)
		return true
	}

	@OptIn(ExperimentalComposeUiApi::class, InternalComposeUiApi::class)
	override fun mouseScrolled(
		mouseX: Double,
		mouseY: Double,
		horizontalAmount: Double,
		verticalAmount: Double
	): Boolean {
		val awtEvent = AwtUtils.createMouseWheelEvent(
			toPhysical(mouseX).toInt(), toPhysical(mouseY).toInt(),
			mouseY.toDouble(), awtMods(), AwtMouseEvent.MOUSE_WHEEL
		)
		composeScene?.sendPointerEvent(
			position = Offset(toPhysical(mouseX), toPhysical(mouseY)),
			eventType = PointerEventType.Scroll,
			scrollDelta = Offset(horizontalAmount.toFloat(), (-verticalAmount).toFloat()),
			nativeEvent = awtEvent
		)
		return true
	}

	@OptIn(InternalComposeUiApi::class)
	override fun charTyped(event: CharacterEvent): Boolean {
		val time = System.nanoTime() / 1_000_000
		composeScene?.sendKeyEvent(
			AwtUtils.createComposeKeyEvent(
				AwtKeyEvent.KEY_TYPED,
				time,
				awtMods(),
				0,
				event.codepoint().toChar(),
				AwtKeyEvent.KEY_LOCATION_UNKNOWN
			)
		)
		return true
	}

	@OptIn(InternalComposeUiApi::class)
	override fun keyPressed(event: McKeyEvent): Boolean {
		if (event.key() == GLFW.GLFW_KEY_ESCAPE && !handleEscape()) {
			onClose()
			return true
		}
		composeScene?.sendKeyEvent(
			AwtUtils.createComposeKeyEvent(
				AwtKeyEvent.KEY_PRESSED,
				System.currentTimeMillis(),
				awtMods(),
				glfwToAwtKeyCode(event.key()),
				AwtKeyEvent.CHAR_UNDEFINED,
				AwtKeyEvent.KEY_LOCATION_STANDARD
			)
		)
		return true
	}

	@OptIn(InternalComposeUiApi::class)
	override fun keyReleased(event: McKeyEvent): Boolean {
		composeScene?.sendKeyEvent(
			AwtUtils.createComposeKeyEvent(
				AwtKeyEvent.KEY_RELEASED,
				System.nanoTime() / 1_000_000,
				awtMods(),
				glfwToAwtKeyCode(event.key()),
				0.toChar(),
				AwtKeyEvent.KEY_LOCATION_STANDARD
			)
		)
		return true
	}

	/**
	 * 子类可拦截 Esc（如取消热键监听）。返回 true 表示已消费，不关闭屏幕。
	 */
	protected open fun handleEscape(): Boolean = false

	override fun removed() {
		ComposeRenderer.screenLayer = null
		composeScene?.close()
		composeScene = null
		super.removed()
	}
}
