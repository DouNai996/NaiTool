package com.naitool.ui.hud

import com.google.gson.GsonBuilder
import fi.dy.masa.malilib.util.FileUtils
import java.nio.file.Files
import java.nio.file.Path

/** 单个 HUD 组件的持久化布局：x/y 为相对锚点角的屏幕比例（0..1），scale 为缩放 */
data class StoredHudPos(var x: Float = 0.012f, var y: Float = 0.012f, var scale: Float = 1f)

private class HudLayoutFile {
	var components: LinkedHashMap<String, StoredHudPos> = LinkedHashMap()
}

/**
 * HUD 布局独立存于 run/config/naitool_hud.json（Gson），
 * 与 MaLiLib 的 naitool.json 互不干扰。
 */
object HudLayoutStore {
	private const val FILE_NAME = "naitool_hud.json"
	private val gson = GsonBuilder().setPrettyPrinting().create()

	private val path: Path
		get() = FileUtils.getConfigDirectory().resolve(FILE_NAME)

	fun load(): Map<String, StoredHudPos> {
		return try {
			val p = path
			if (!Files.exists(p) || !Files.isReadable(p)) return emptyMap()
			val data = gson.fromJson(Files.readString(p), HudLayoutFile::class.java) ?: return emptyMap()
			data.components
		} catch (e: Exception) {
			emptyMap()
		}
	}

	fun save(components: Map<String, StoredHudPos>) {
		try {
			val dir = FileUtils.getConfigDirectory()
			if (!Files.exists(dir)) Files.createDirectories(dir)
			val data = HudLayoutFile()
			data.components.putAll(components)
			Files.writeString(path, gson.toJson(data))
		} catch (_: Exception) {
			// 布局写盘失败不影响游戏
		}
	}
}
