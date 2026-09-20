package com.naitool.ui.hud

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.naitool.config.Configs
import fi.dy.masa.malilib.util.StringUtils

/** 一条通知。detail != null 时为自定义文案通知，否则为功能开关通知 */
class Notification(
	val id: Long,
	val title: String,
	val enabled: Boolean,
	val bornAt: Long,
	val detail: String? = null,
	val ttlMs: Long = 1800L
) {
	var visible by mutableStateOf(false)
	var expiryAt by mutableLongStateOf(0L)
}

object NotificationManager {
	private const val TTL_MS = 1800L
	private const val CUSTOM_TTL_MS = 2600L
	private const val EXIT_ANIM_MS = 200L
	private const val MAX_ITEMS = 5

	val items = mutableStateListOf<Notification>()
	private var nextId = 1L

	fun toggle(title: String, enabled: Boolean) {
		if (!Configs.Generic.INTERFACE_NOTIFICATIONS.booleanValue) return
		val now = System.currentTimeMillis()
		val n = Notification(nextId++, title, enabled, now)
		n.expiryAt = now + TTL_MS
		items.add(n)
		while (items.size > MAX_ITEMS) items.removeAt(0)
	}

	/** 自定义文案通知（如鞘翅加速起飞提示），停留时间稍长便于阅读 */
	fun show(title: String, detail: String) {
		if (!Configs.Generic.INTERFACE_NOTIFICATIONS.booleanValue) return
		if (detail.isBlank()) return
		val now = System.currentTimeMillis()
		val n = Notification(nextId++, title, true, now, detail = detail, ttlMs = CUSTOM_TTL_MS)
		n.expiryAt = now + CUSTOM_TTL_MS
		items.add(n)
		while (items.size > MAX_ITEMS) items.removeAt(0)
	}

	/** 每帧/轮询时清理过期通知；返回需要播放退出动画的通知由 Composable 自行处理 */
	fun prune(now: Long) {
		items.removeAll { now - it.bornAt > it.ttlMs + EXIT_ANIM_MS }
	}
}

/**
 * 功能开关通知的统一入口（Java 热键回调与 Compose ClickGui 均走这里）。
 * prefix 对应 ConfigBridge 的分组前缀：elytraBoost / elytraTrails / ...
 */
object FeatureNotifier {
	private val TITLE_KEYS = mapOf(
		"elytraBoost" to "naitool.ui.clickgui.category.boost",
		"elytraTrails" to "naitool.ui.clickgui.category.trails",
		"nightVision" to "naitool.ui.clickgui.category.nightVision",
		"sprint" to "naitool.ui.clickgui.category.sprint",
		"freeCamera" to "naitool.ui.clickgui.category.freeCamera",
		"ghostMine" to "naitool.ui.clickgui.category.ghostMine",
		"noFall" to "naitool.ui.clickgui.category.noFall"
	)

	@JvmStatic
	fun onFeatureToggled(prefix: String, enabled: Boolean) {
		val key = TITLE_KEYS[prefix] ?: return
		NotificationManager.toggle(StringUtils.translate(key), enabled)
	}

	/** 鞘翅加速成功起飞时弹出用户自定义文案 */
	@JvmStatic
	fun onBoost(message: String?) {
		if (message.isNullOrBlank()) return
		NotificationManager.show(StringUtils.translate(TITLE_KEYS.getValue("elytraBoost")), message)
	}
}
