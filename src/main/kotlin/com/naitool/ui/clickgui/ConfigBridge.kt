package com.naitool.ui.clickgui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.naitool.config.Configs
import com.naitool.config.Hotkeys
import com.naitool.ui.hud.FeatureNotifier
import fi.dy.masa.malilib.config.IConfigBase
import fi.dy.masa.malilib.config.IConfigOptionListEntry
import fi.dy.masa.malilib.config.options.ConfigBoolean
import fi.dy.masa.malilib.config.options.ConfigDouble
import fi.dy.masa.malilib.config.options.ConfigHotkey
import fi.dy.masa.malilib.config.options.ConfigInteger
import fi.dy.masa.malilib.config.options.ConfigOptionList
import fi.dy.masa.malilib.config.options.ConfigString
import fi.dy.masa.malilib.util.StringUtils

/** 清理 MaLiLib 文案：去 § 颜色码、去换行 */
fun cleanConfigText(raw: String?): String {
	if (raw.isNullOrEmpty()) return ""
	return raw.replace(Regex("§."), "")
		.substringBefore('\n')
		.trim()
}

private fun displayName(option: IConfigBase): String {
	val translated = option.configGuiDisplayName
	// 缺少翻译时 MaLiLib 返回翻译键本身（含 '.'）
	return if (translated.isNullOrEmpty() || translated.contains('.')) {
		option.prettyName ?: option.name
	} else translated
}

private fun firstCommentLine(option: IConfigBase): String =
	cleanConfigText(option.comment)

sealed class SettingModel {
	abstract val displayName: String
	abstract val description: String
}

class BooleanModel(
	val option: ConfigBoolean,
	val featurePrefix: String? = null
) : SettingModel() {
	override val displayName: String = displayName(option)
	override val description: String = firstCommentLine(option)

	var checked by mutableStateOf(option.booleanValue)

	fun set(value: Boolean) {
		option.setBooleanValue(value)
		checked = value
		ConfigBridge.save()
		if (featurePrefix != null) FeatureNotifier.onFeatureToggled(featurePrefix, value)
	}
}

class NumberModel(val option: IConfigBase, val integer: Boolean, val min: Float, val max: Float) : SettingModel() {
	override val displayName: String = displayName(option)
	override val description: String = firstCommentLine(option)

	var value by mutableStateOf(
		if (integer) (option as ConfigInteger).integerValue.toFloat()
		else (option as ConfigDouble).doubleValue.toFloat()
	)

	fun set(newValue: Float, commit: Boolean) {
		val clamped = newValue.coerceIn(min, max)
		value = clamped
		if (integer) {
			(option as ConfigInteger).setIntegerValue(clamped.toInt())
		} else {
			(option as ConfigDouble).setDoubleValue(clamped.toDouble())
		}
		if (commit) ConfigBridge.save()
	}

	fun formatValue(): String =
		if (integer) value.toInt().toString() else String.format("%.2f", value)
}

class ModeModel(val option: ConfigOptionList, val entries: List<IConfigOptionListEntry>) : SettingModel() {
	override val displayName: String = displayName(option)
	override val description: String = firstCommentLine(option)

	var selected by mutableStateOf(option.optionListValue)

	fun select(entry: IConfigOptionListEntry) {
		option.setOptionListValue(entry)
		selected = entry
		ConfigBridge.save()
	}
}

/** RGB 颜色（MaLiLib ConfigInteger，范围 0..0xFFFFFF） */
class ColorModel(val option: ConfigInteger) : SettingModel() {
	override val displayName: String = displayName(option)
	override val description: String = firstCommentLine(option)

	var rgb by mutableStateOf(option.integerValue and 0xFFFFFF)

	fun set(newRgb: Int) {
		rgb = newRgb and 0xFFFFFF
		option.setIntegerValue(rgb)
	}

	fun commit() = ConfigBridge.save()
}

/** 单行文本（MaLiLib ConfigString，如起飞通知文案） */
class StringModel(val option: ConfigString) : SettingModel() {
	override val displayName: String = displayName(option)
	override val description: String = firstCommentLine(option)

	var text by mutableStateOf(option.stringValue)

	fun set(newText: String) {
		option.setStringValue(newText)
		text = newText
	}

	fun commit() = ConfigBridge.save()
}

class KeybindModel(val hotkey: ConfigHotkey) {
	val displayName: String = displayName(hotkey)
	val description: String = firstCommentLine(hotkey)

	fun keyDisplay(): String {
		val text = hotkey.keybind.keysDisplayString
		return if (text.isNullOrEmpty()) StringUtils.translate("naitool.ui.clickgui.none") else text
	}

	fun bind(glfwKey: Int) {
		hotkey.keybind.clearKeys()
		hotkey.keybind.addKey(glfwKey)
		ConfigBridge.save()
	}

	fun unbind() {
		hotkey.keybind.clearKeys()
		ConfigBridge.save()
	}
}

/** 当前正在监听按键的热键（全局同一时刻仅一个） */
object KeybindListener {
	var target by mutableStateOf<KeybindModel?>(null)
}

/** 一个功能（模块）：开关 + 若干设置 + 可选的内嵌快捷键 */
data class ModuleModel(
	val title: String,
	val toggle: BooleanModel?,
	val settings: List<SettingModel>,
	val keybind: KeybindModel? = null
)

/** 一个分类面板（移动 / 视觉 / 实用 / 界面），内含多个功能 */
data class CategoryModel(
	val id: String,
	val title: String,
	val modules: List<ModuleModel>
)

object ConfigBridge {
	private data class FeatureDef(
		val prefix: String,
		val titleKey: String,
		val hotkey: ConfigHotkey? = null
	)

	private data class CategoryDef(
		val id: String,
		val titleKey: String,
		val features: List<FeatureDef>
	)

	private val CATEGORY_DEFS = listOf(
		CategoryDef(
			"movement", "naitool.ui.clickgui.category.movement",
			listOf(
				FeatureDef("elytraBoost", "naitool.ui.clickgui.category.boost", Hotkeys.ELYTRA_BOOST),
				FeatureDef("sprint", "naitool.ui.clickgui.category.sprint"),
				FeatureDef("noFall", "naitool.ui.clickgui.category.noFall", Hotkeys.NO_FALL_TOGGLE)
			)
		),
		CategoryDef(
			"render", "naitool.ui.clickgui.category.render",
			listOf(
				FeatureDef("nightVision", "naitool.ui.clickgui.category.nightVision"),
				FeatureDef("elytraTrails", "naitool.ui.clickgui.category.trails"),
				FeatureDef("freeCamera", "naitool.ui.clickgui.category.freeCamera")
			)
		),
		CategoryDef(
			"utility", "naitool.ui.clickgui.category.utility",
			listOf(
				FeatureDef("ghostMine", "naitool.ui.clickgui.category.ghostMine", Hotkeys.GHOST_MINE_TOGGLE)
			)
		)
	)

	fun save() {
		Configs.saveToFile()
	}

	/** 每次打开 ClickGui 时构建一次（状态镜像） */
	fun build(): List<CategoryModel> {
		val groups = CATEGORY_DEFS.map { cat ->
			CategoryModel(
				id = cat.id,
				title = StringUtils.translate(cat.titleKey),
				modules = cat.features.map { buildFeatureModule(it) }
			)
		}
		return groups + buildInterfaceCategory()
	}

	private fun buildFeatureModule(def: FeatureDef): ModuleModel {
		val settings = mutableListOf<SettingModel>()
		var toggle: BooleanModel? = null

		Configs.Generic.OPTIONS
			.filter { it.name.startsWith(def.prefix) }
			.forEach { option ->
				if (option is ConfigBoolean && option.name == "${def.prefix}Enabled") {
					toggle = BooleanModel(option, def.prefix)
				} else {
					optionModel(option)?.let { settings.add(it) }
				}
			}

		return ModuleModel(
			title = StringUtils.translate(def.titleKey),
			toggle = toggle,
			settings = settings,
			keybind = def.hotkey?.let { KeybindModel(it) }
		)
	}

	/** 界面分类：没有按功能拆分，整体作为一个模块；打开 GUI 的热键内嵌其中 */
	private fun buildInterfaceCategory(): CategoryModel {
		val settings = mutableListOf<SettingModel>()
		var toggle: BooleanModel? = null

		Configs.Generic.OPTIONS
			.filter { it.name.startsWith("interface") }
			.forEach { option ->
				if (option is ConfigBoolean && option.name == "interfaceEnabled") {
					toggle = BooleanModel(option, null)
				} else {
					optionModel(option)?.let { settings.add(it) }
				}
			}

		val module = ModuleModel(
			title = StringUtils.translate("naitool.ui.clickgui.category.interface"),
			toggle = toggle,
			settings = settings,
			keybind = KeybindModel(Hotkeys.OPEN_CONFIG_GUI)
		)
		return CategoryModel(
			id = "interface",
			title = StringUtils.translate("naitool.ui.clickgui.category.interface"),
			modules = listOf(module)
		)
	}

	private fun optionModel(option: IConfigBase): SettingModel? = when (option) {
		is ConfigBoolean -> BooleanModel(option, null)
		is ConfigString -> StringModel(option)
		is ConfigInteger -> {
			if (option.maxIntegerValue == 0xFFFFFF && option.minIntegerValue == 0) {
				ColorModel(option)
			} else {
				NumberModel(
					option, true,
					option.minIntegerValue.toFloat(),
					option.maxIntegerValue.toFloat()
				)
			}
		}
		is ConfigDouble -> NumberModel(
			option, false,
			option.minDoubleValue.toFloat(),
			option.maxDoubleValue.toFloat()
		)
		is ConfigOptionList -> {
			@Suppress("UNCHECKED_CAST")
			val enumConstants: List<IConfigOptionListEntry> =
				option.optionListValue?.javaClass?.enumConstants
					?.mapNotNull { it as? IConfigOptionListEntry }
					?: emptyList()
			ModeModel(option, enumConstants)
		}
		else -> null
	}
}
