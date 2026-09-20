package com.naitool.ui.hud

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/** 一个 HUD 组件的实时布局状态（屏幕比例坐标） */
class HudPosState(x: Float, y: Float, scale: Float) {
	var x by mutableStateOf(x)
	var y by mutableStateOf(y)
	var scale by mutableStateOf(scale)
}

/** HUD 编辑器：编辑态、选中组件、各组件位置状态（对标参考项目 HudEditorManager） */
object HudEditorManager {
	var editMode by mutableStateOf(false)
	var selectedId by mutableStateOf<String?>(null)

	private val states = mutableStateMapOf<String, HudPosState>()
	private val stored: Map<String, StoredHudPos> = HudLayoutStore.load()

	fun stateFor(id: String, default: StoredHudPos = StoredHudPos()): HudPosState {
		return states.getOrPut(id) {
			val s = stored[id] ?: default
			HudPosState(s.x, s.y, s.scale)
		}
	}

	fun enterEditMode() {
		editMode = true
	}

	fun exitEditMode() {
		editMode = false
		selectedId = null
		persist()
	}

	fun toggleEditMode() {
		if (editMode) exitEditMode() else enterEditMode()
	}

	private fun persist() {
		val map = states.mapValues { StoredHudPos(it.value.x, it.value.y, it.value.scale) }
		HudLayoutStore.save(map)
	}
}
