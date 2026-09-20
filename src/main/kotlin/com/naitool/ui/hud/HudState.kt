package com.naitool.ui.hud

import androidx.compose.ui.graphics.Color
import com.naitool.config.Configs
import com.naitool.config.InterfaceColorMode
import fi.dy.masa.malilib.config.IConfigOptionListEntry
import fi.dy.masa.malilib.config.options.ConfigBoolean
import fi.dy.masa.malilib.config.options.ConfigOptionList
import fi.dy.masa.malilib.util.StringUtils
import kotlin.math.sin

/** 右上角 ArrayList 中的一行：功能名 + 可选模式后缀 */
data class HudFeature(val title: String, val modeText: String?) {
	val fullText: String get() = if (modeText.isNullOrEmpty()) title else "$title $modeText"
}

private data class FeatureDef(
	val titleKey: String,
	val enabled: ConfigBoolean,
	val mode: ConfigOptionList?
)

/**
 * HUD 数据快照：直接读 MaLiLib 配置（热键/GUI 改的都是同一份值），
 * 由 HudRoot 定时轮询，不需要事件订阅。
 */
object HudState {
	private val FEATURES = listOf(
		FeatureDef("naitool.ui.clickgui.category.boost", Configs.Generic.ELYTRA_BOOST_ENABLED, null),
		FeatureDef("naitool.ui.clickgui.category.trails", Configs.Generic.ELYTRA_TRAILS_ENABLED, Configs.Generic.ELYTRA_TRAILS_MODE),
		FeatureDef("naitool.ui.clickgui.category.nightVision", Configs.Generic.NIGHT_VISION_ENABLED, Configs.Generic.NIGHT_VISION_MODE),
		FeatureDef("naitool.ui.clickgui.category.sprint", Configs.Generic.SPRINT_ENABLED, Configs.Generic.SPRINT_MODE),
		FeatureDef("naitool.ui.clickgui.category.freeCamera", Configs.Generic.FREE_CAMERA_ENABLED, null),
		FeatureDef("naitool.ui.clickgui.category.ghostMine", Configs.Generic.GHOST_MINE_ENABLED, null),
		FeatureDef("naitool.ui.clickgui.category.noFall", Configs.Generic.NO_FALL_ENABLED, null)
	)

	private fun entryDisplayName(entry: IConfigOptionListEntry?): String? {
		if (entry == null) return null
		val name = entry.displayName
		return if (name.isNullOrEmpty() || name.contains('.')) entry.stringValue else name
	}

	fun enabledFeatures(): List<HudFeature> = FEATURES.mapNotNull { def ->
		if (!def.enabled.booleanValue) return@mapNotNull null
		HudFeature(
			title = StringUtils.translate(def.titleKey),
			modeText = entryDisplayName(def.mode?.optionListValue)?.let { "| $it" }
		)
	}

	fun hudEnabled(): Boolean = Configs.Generic.INTERFACE_ENABLED.booleanValue
	fun watermarkEnabled(): Boolean = Configs.Generic.INTERFACE_WATERMARK.booleanValue
	fun backgroundEnabled(): Boolean = Configs.Generic.INTERFACE_BACKGROUND.booleanValue
	fun fontSize(): Int = Configs.Generic.INTERFACE_FONT_SIZE.integerValue

	/** 参考项目 Hud.kt 的四种配色（彩虹/纯色/明暗/渐变） */
	fun rowColor(index: Int, total: Int, hueOffset: Float, timeMs: Long): Color {
		val mode = Configs.Generic.INTERFACE_COLOR_MODE.optionListValue as? InterfaceColorMode
			?: InterfaceColorMode.RAINBOW
		return when (mode) {
			InterfaceColorMode.RAINBOW -> {
				val h = (hueOffset + index * 0.05f) % 1f
				Color.hsv(h * 360f, 0.6f, 1f)
			}
			InterfaceColorMode.STATIC -> Color(108, 53, 222)
			InterfaceColorMode.FADE -> {
				val alpha = (sin(timeMs / 1000.0 + index * 0.3) * 0.3 + 0.7).toFloat()
				Color.White.copy(alpha = alpha.coerceIn(0.4f, 1f))
			}
			InterfaceColorMode.GRADIENT -> {
				val fraction = if (total > 1) index.toFloat() / (total - 1) else 0f
				lerpColor(Color(108, 53, 222), Color(0xFF, 0x6B, 0x6B), fraction)
			}
		}
	}

	private fun lerpColor(start: Color, end: Color, fraction: Float): Color = Color(
		red = start.red + (end.red - start.red) * fraction,
		green = start.green + (end.green - start.green) * fraction,
		blue = start.blue + (end.blue - start.blue) * fraction,
		alpha = 1f
	)
}
