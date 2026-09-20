package com.naitool.ui.core

import androidx.compose.ui.InternalComposeUiApi
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import net.minecraft.client.Minecraft
import org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_ALT
import org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_CONTROL
import org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_SHIFT
import org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT_ALT
import org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT_CONTROL
import org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT_SHIFT
import org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_1
import org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_2
import org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_3
import org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_4
import org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_5
import org.lwjgl.glfw.GLFW.GLFW_PRESS
import org.lwjgl.glfw.GLFW.glfwGetKey
import org.lwjgl.glfw.GLFW.glfwGetMouseButton
import java.awt.Component
import java.awt.event.InputEvent
import java.awt.event.KeyEvent as AwtKeyEvent
import java.awt.event.MouseEvent
import java.awt.event.MouseWheelEvent

// from https://github.com/JetBrains/compose-multiplatform/blob/master/experimental/lwjgl-integration/src/main/kotlin/GlfwEvents.kt
internal object AwtUtils {
	val awtComponent = object : Component() {}

	fun getAwtMods(windowHandle: Long): Int {
		var awtMods = 0
		if (glfwGetMouseButton(windowHandle, GLFW_MOUSE_BUTTON_1) == GLFW_PRESS)
			awtMods = awtMods or InputEvent.BUTTON1_DOWN_MASK
		if (glfwGetMouseButton(windowHandle, GLFW_MOUSE_BUTTON_2) == GLFW_PRESS)
			awtMods = awtMods or InputEvent.BUTTON2_DOWN_MASK
		if (glfwGetMouseButton(windowHandle, GLFW_MOUSE_BUTTON_3) == GLFW_PRESS)
			awtMods = awtMods or InputEvent.BUTTON3_DOWN_MASK
		if (glfwGetMouseButton(windowHandle, GLFW_MOUSE_BUTTON_4) == GLFW_PRESS)
			awtMods = awtMods or (1 shl 14)
		if (glfwGetMouseButton(windowHandle, GLFW_MOUSE_BUTTON_5) == GLFW_PRESS)
			awtMods = awtMods or (1 shl 15)
		if (glfwGetKey(windowHandle, GLFW_KEY_LEFT_CONTROL) == GLFW_PRESS ||
			glfwGetKey(windowHandle, GLFW_KEY_RIGHT_CONTROL) == GLFW_PRESS)
			awtMods = awtMods or InputEvent.CTRL_DOWN_MASK
		if (glfwGetKey(windowHandle, GLFW_KEY_LEFT_SHIFT) == GLFW_PRESS ||
			glfwGetKey(windowHandle, GLFW_KEY_RIGHT_SHIFT) == GLFW_PRESS)
			awtMods = awtMods or InputEvent.SHIFT_DOWN_MASK
		if (glfwGetKey(windowHandle, GLFW_KEY_LEFT_ALT) == GLFW_PRESS ||
			glfwGetKey(windowHandle, GLFW_KEY_RIGHT_ALT) == GLFW_PRESS)
			awtMods = awtMods or InputEvent.ALT_DOWN_MASK
		return awtMods
	}

	fun glfwToAwtButton(glfwButton: Int): Int = when (glfwButton) {
		GLFW_MOUSE_BUTTON_1 -> MouseEvent.BUTTON1
		GLFW_MOUSE_BUTTON_2 -> MouseEvent.BUTTON2
		GLFW_MOUSE_BUTTON_3 -> MouseEvent.BUTTON3
		else -> MouseEvent.BUTTON1
	}

	fun createMouseEvent(mouseX: Int, mouseY: Int, awtMods: Int, button: Int, eventType: Int) = MouseEvent(
		awtComponent, eventType, System.currentTimeMillis(), awtMods, mouseX, mouseY, 1, false,
		glfwToAwtButton(button)
	)

	fun createMouseWheelEvent(x: Int, y: Int, scrollY: Double, awtMods: Int, eventType: Int) =
		MouseWheelEvent(
			awtComponent,
			eventType, System.currentTimeMillis(),
			awtMods,
			x, y,
			0,
			false,
			MouseWheelEvent.WHEEL_UNIT_SCROLL,
			1,
			(-scrollY).toInt()
		)

	private fun isCtrlPressed(windowHandle: Long): Boolean {
		return glfwGetKey(windowHandle, GLFW_KEY_LEFT_CONTROL) == GLFW_PRESS ||
			glfwGetKey(windowHandle, GLFW_KEY_RIGHT_CONTROL) == GLFW_PRESS
	}

	private fun isShiftPressed(windowHandle: Long): Boolean {
		return glfwGetKey(windowHandle, GLFW_KEY_LEFT_SHIFT) == GLFW_PRESS ||
			glfwGetKey(windowHandle, GLFW_KEY_RIGHT_SHIFT) == GLFW_PRESS
	}

	private fun isAltPressed(windowHandle: Long): Boolean {
		return glfwGetKey(windowHandle, GLFW_KEY_LEFT_ALT) == GLFW_PRESS ||
			glfwGetKey(windowHandle, GLFW_KEY_RIGHT_ALT) == GLFW_PRESS
	}

	@OptIn(InternalComposeUiApi::class)
	fun createComposeKeyEvent(awtId: Int, time: Long, awtMods: Int, key: Int, char: Char, location: Int): KeyEvent {
		val handle = Minecraft.getInstance().window.handle()
		return KeyEvent(
			key = Key(key, location),
			type = when (awtId) {
				AwtKeyEvent.KEY_PRESSED -> androidx.compose.ui.input.key.KeyEventType.KeyDown
				AwtKeyEvent.KEY_RELEASED -> androidx.compose.ui.input.key.KeyEventType.KeyUp
				else -> androidx.compose.ui.input.key.KeyEventType.Unknown
			},
			codePoint = char.code,
			nativeEvent = AwtKeyEvent(awtComponent, awtId, time, awtMods, key, char, location),
			isCtrlPressed = isCtrlPressed(handle),
			isAltPressed = isAltPressed(handle),
			isShiftPressed = isShiftPressed(handle)
		)
	}
}

fun glfwToAwtKeyCode(glfwKeyCode: Int): Int = when (glfwKeyCode) {
	org.lwjgl.glfw.GLFW.GLFW_KEY_SPACE -> AwtKeyEvent.VK_SPACE
	org.lwjgl.glfw.GLFW.GLFW_KEY_APOSTROPHE -> AwtKeyEvent.VK_QUOTE
	org.lwjgl.glfw.GLFW.GLFW_KEY_COMMA -> AwtKeyEvent.VK_COMMA
	org.lwjgl.glfw.GLFW.GLFW_KEY_MINUS -> AwtKeyEvent.VK_MINUS
	org.lwjgl.glfw.GLFW.GLFW_KEY_PERIOD -> AwtKeyEvent.VK_PERIOD
	org.lwjgl.glfw.GLFW.GLFW_KEY_SLASH -> AwtKeyEvent.VK_SLASH
	org.lwjgl.glfw.GLFW.GLFW_KEY_0 -> AwtKeyEvent.VK_0
	org.lwjgl.glfw.GLFW.GLFW_KEY_1 -> AwtKeyEvent.VK_1
	org.lwjgl.glfw.GLFW.GLFW_KEY_2 -> AwtKeyEvent.VK_2
	org.lwjgl.glfw.GLFW.GLFW_KEY_3 -> AwtKeyEvent.VK_3
	org.lwjgl.glfw.GLFW.GLFW_KEY_4 -> AwtKeyEvent.VK_4
	org.lwjgl.glfw.GLFW.GLFW_KEY_5 -> AwtKeyEvent.VK_5
	org.lwjgl.glfw.GLFW.GLFW_KEY_6 -> AwtKeyEvent.VK_6
	org.lwjgl.glfw.GLFW.GLFW_KEY_7 -> AwtKeyEvent.VK_7
	org.lwjgl.glfw.GLFW.GLFW_KEY_8 -> AwtKeyEvent.VK_8
	org.lwjgl.glfw.GLFW.GLFW_KEY_9 -> AwtKeyEvent.VK_9
	org.lwjgl.glfw.GLFW.GLFW_KEY_SEMICOLON -> AwtKeyEvent.VK_SEMICOLON
	org.lwjgl.glfw.GLFW.GLFW_KEY_EQUAL -> AwtKeyEvent.VK_EQUALS
	org.lwjgl.glfw.GLFW.GLFW_KEY_A -> AwtKeyEvent.VK_A
	org.lwjgl.glfw.GLFW.GLFW_KEY_B -> AwtKeyEvent.VK_B
	org.lwjgl.glfw.GLFW.GLFW_KEY_C -> AwtKeyEvent.VK_C
	org.lwjgl.glfw.GLFW.GLFW_KEY_D -> AwtKeyEvent.VK_D
	org.lwjgl.glfw.GLFW.GLFW_KEY_E -> AwtKeyEvent.VK_E
	org.lwjgl.glfw.GLFW.GLFW_KEY_F -> AwtKeyEvent.VK_F
	org.lwjgl.glfw.GLFW.GLFW_KEY_G -> AwtKeyEvent.VK_G
	org.lwjgl.glfw.GLFW.GLFW_KEY_H -> AwtKeyEvent.VK_H
	org.lwjgl.glfw.GLFW.GLFW_KEY_I -> AwtKeyEvent.VK_I
	org.lwjgl.glfw.GLFW.GLFW_KEY_J -> AwtKeyEvent.VK_J
	org.lwjgl.glfw.GLFW.GLFW_KEY_K -> AwtKeyEvent.VK_K
	org.lwjgl.glfw.GLFW.GLFW_KEY_L -> AwtKeyEvent.VK_L
	org.lwjgl.glfw.GLFW.GLFW_KEY_M -> AwtKeyEvent.VK_M
	org.lwjgl.glfw.GLFW.GLFW_KEY_N -> AwtKeyEvent.VK_N
	org.lwjgl.glfw.GLFW.GLFW_KEY_O -> AwtKeyEvent.VK_O
	org.lwjgl.glfw.GLFW.GLFW_KEY_P -> AwtKeyEvent.VK_P
	org.lwjgl.glfw.GLFW.GLFW_KEY_Q -> AwtKeyEvent.VK_Q
	org.lwjgl.glfw.GLFW.GLFW_KEY_R -> AwtKeyEvent.VK_R
	org.lwjgl.glfw.GLFW.GLFW_KEY_S -> AwtKeyEvent.VK_S
	org.lwjgl.glfw.GLFW.GLFW_KEY_T -> AwtKeyEvent.VK_T
	org.lwjgl.glfw.GLFW.GLFW_KEY_U -> AwtKeyEvent.VK_U
	org.lwjgl.glfw.GLFW.GLFW_KEY_V -> AwtKeyEvent.VK_V
	org.lwjgl.glfw.GLFW.GLFW_KEY_W -> AwtKeyEvent.VK_W
	org.lwjgl.glfw.GLFW.GLFW_KEY_X -> AwtKeyEvent.VK_X
	org.lwjgl.glfw.GLFW.GLFW_KEY_Y -> AwtKeyEvent.VK_Y
	org.lwjgl.glfw.GLFW.GLFW_KEY_Z -> AwtKeyEvent.VK_Z
	org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_BRACKET -> AwtKeyEvent.VK_OPEN_BRACKET
	org.lwjgl.glfw.GLFW.GLFW_KEY_BACKSLASH -> AwtKeyEvent.VK_BACK_SLASH
	org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT_BRACKET -> AwtKeyEvent.VK_CLOSE_BRACKET
	org.lwjgl.glfw.GLFW.GLFW_KEY_GRAVE_ACCENT -> AwtKeyEvent.VK_BACK_QUOTE
	org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE -> AwtKeyEvent.VK_ESCAPE
	org.lwjgl.glfw.GLFW.GLFW_KEY_ENTER -> AwtKeyEvent.VK_ENTER
	org.lwjgl.glfw.GLFW.GLFW_KEY_TAB -> AwtKeyEvent.VK_TAB
	org.lwjgl.glfw.GLFW.GLFW_KEY_BACKSPACE -> AwtKeyEvent.VK_BACK_SPACE
	org.lwjgl.glfw.GLFW.GLFW_KEY_INSERT -> AwtKeyEvent.VK_INSERT
	org.lwjgl.glfw.GLFW.GLFW_KEY_DELETE -> AwtKeyEvent.VK_DELETE
	org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT -> AwtKeyEvent.VK_RIGHT
	org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT -> AwtKeyEvent.VK_LEFT
	org.lwjgl.glfw.GLFW.GLFW_KEY_DOWN -> AwtKeyEvent.VK_DOWN
	org.lwjgl.glfw.GLFW.GLFW_KEY_UP -> AwtKeyEvent.VK_UP
	org.lwjgl.glfw.GLFW.GLFW_KEY_PAGE_UP -> AwtKeyEvent.VK_PAGE_UP
	org.lwjgl.glfw.GLFW.GLFW_KEY_PAGE_DOWN -> AwtKeyEvent.VK_PAGE_DOWN
	org.lwjgl.glfw.GLFW.GLFW_KEY_HOME -> AwtKeyEvent.VK_HOME
	org.lwjgl.glfw.GLFW.GLFW_KEY_END -> AwtKeyEvent.VK_END
	org.lwjgl.glfw.GLFW.GLFW_KEY_CAPS_LOCK -> AwtKeyEvent.VK_CAPS_LOCK
	org.lwjgl.glfw.GLFW.GLFW_KEY_SCROLL_LOCK -> AwtKeyEvent.VK_SCROLL_LOCK
	org.lwjgl.glfw.GLFW.GLFW_KEY_NUM_LOCK -> AwtKeyEvent.VK_NUM_LOCK
	org.lwjgl.glfw.GLFW.GLFW_KEY_PRINT_SCREEN -> AwtKeyEvent.VK_PRINTSCREEN
	org.lwjgl.glfw.GLFW.GLFW_KEY_PAUSE -> AwtKeyEvent.VK_PAUSE
	org.lwjgl.glfw.GLFW.GLFW_KEY_F1 -> AwtKeyEvent.VK_F1
	org.lwjgl.glfw.GLFW.GLFW_KEY_F2 -> AwtKeyEvent.VK_F2
	org.lwjgl.glfw.GLFW.GLFW_KEY_F3 -> AwtKeyEvent.VK_F3
	org.lwjgl.glfw.GLFW.GLFW_KEY_F4 -> AwtKeyEvent.VK_F4
	org.lwjgl.glfw.GLFW.GLFW_KEY_F5 -> AwtKeyEvent.VK_F5
	org.lwjgl.glfw.GLFW.GLFW_KEY_F6 -> AwtKeyEvent.VK_F6
	org.lwjgl.glfw.GLFW.GLFW_KEY_F7 -> AwtKeyEvent.VK_F7
	org.lwjgl.glfw.GLFW.GLFW_KEY_F8 -> AwtKeyEvent.VK_F8
	org.lwjgl.glfw.GLFW.GLFW_KEY_F9 -> AwtKeyEvent.VK_F9
	org.lwjgl.glfw.GLFW.GLFW_KEY_F10 -> AwtKeyEvent.VK_F10
	org.lwjgl.glfw.GLFW.GLFW_KEY_F11 -> AwtKeyEvent.VK_F11
	org.lwjgl.glfw.GLFW.GLFW_KEY_F12 -> AwtKeyEvent.VK_F12
	org.lwjgl.glfw.GLFW.GLFW_KEY_F13 -> AwtKeyEvent.VK_F13
	org.lwjgl.glfw.GLFW.GLFW_KEY_F14 -> AwtKeyEvent.VK_F14
	org.lwjgl.glfw.GLFW.GLFW_KEY_F15 -> AwtKeyEvent.VK_F15
	org.lwjgl.glfw.GLFW.GLFW_KEY_F16 -> AwtKeyEvent.VK_F16
	org.lwjgl.glfw.GLFW.GLFW_KEY_F17 -> AwtKeyEvent.VK_F17
	org.lwjgl.glfw.GLFW.GLFW_KEY_F18 -> AwtKeyEvent.VK_F18
	org.lwjgl.glfw.GLFW.GLFW_KEY_F19 -> AwtKeyEvent.VK_F19
	org.lwjgl.glfw.GLFW.GLFW_KEY_F20 -> AwtKeyEvent.VK_F20
	org.lwjgl.glfw.GLFW.GLFW_KEY_F21 -> AwtKeyEvent.VK_F21
	org.lwjgl.glfw.GLFW.GLFW_KEY_F22 -> AwtKeyEvent.VK_F22
	org.lwjgl.glfw.GLFW.GLFW_KEY_F23 -> AwtKeyEvent.VK_F23
	org.lwjgl.glfw.GLFW.GLFW_KEY_F24 -> AwtKeyEvent.VK_F24
	org.lwjgl.glfw.GLFW.GLFW_KEY_KP_0 -> AwtKeyEvent.VK_NUMPAD0
	org.lwjgl.glfw.GLFW.GLFW_KEY_KP_1 -> AwtKeyEvent.VK_NUMPAD1
	org.lwjgl.glfw.GLFW.GLFW_KEY_KP_2 -> AwtKeyEvent.VK_NUMPAD2
	org.lwjgl.glfw.GLFW.GLFW_KEY_KP_3 -> AwtKeyEvent.VK_NUMPAD3
	org.lwjgl.glfw.GLFW.GLFW_KEY_KP_4 -> AwtKeyEvent.VK_NUMPAD4
	org.lwjgl.glfw.GLFW.GLFW_KEY_KP_5 -> AwtKeyEvent.VK_NUMPAD5
	org.lwjgl.glfw.GLFW.GLFW_KEY_KP_6 -> AwtKeyEvent.VK_NUMPAD6
	org.lwjgl.glfw.GLFW.GLFW_KEY_KP_7 -> AwtKeyEvent.VK_NUMPAD7
	org.lwjgl.glfw.GLFW.GLFW_KEY_KP_8 -> AwtKeyEvent.VK_NUMPAD8
	org.lwjgl.glfw.GLFW.GLFW_KEY_KP_9 -> AwtKeyEvent.VK_NUMPAD9
	org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_SHIFT -> AwtKeyEvent.VK_SHIFT
	org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_CONTROL -> AwtKeyEvent.VK_CONTROL
	org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_ALT -> AwtKeyEvent.VK_ALT
	org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT_SHIFT -> AwtKeyEvent.VK_SHIFT
	org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT_CONTROL -> AwtKeyEvent.VK_CONTROL
	org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT_ALT -> AwtKeyEvent.VK_ALT
	else -> AwtKeyEvent.VK_UNDEFINED
}
