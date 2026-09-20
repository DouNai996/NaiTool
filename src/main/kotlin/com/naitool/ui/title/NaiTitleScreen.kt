package com.naitool.ui.title

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.decodeToImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.naitool.ui.clickgui.ClickGuiScreen
import fi.dy.masa.malilib.util.StringUtils
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen
import net.minecraft.client.gui.screens.options.OptionsScreen
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen
import net.minecraft.network.chat.Component
import kotlin.math.sqrt
import kotlin.random.Random
import com.naitool.ui.core.ComposeScreen

/**
 * 自定义标题屏（移植自参考项目 SuperSoft TitleScreen）：
 * 背景图 + 漂浮粒子连线 + 淡入缩放的标题与菜单。
 */
class NaiTitleScreen : ComposeScreen(Component.literal("NaiTool Title")) {

	private var particles: List<TitleParticle> = List(80) { TitleParticle() }

	@Composable
	override fun renderCompose() {
		var visible by remember { mutableStateOf(false) }
		var lastW by remember { mutableStateOf(0) }
		var lastH by remember { mutableStateOf(0) }

		val window = Minecraft.getInstance().window

		LaunchedEffect(Unit) { visible = true }
		LaunchedEffect(Unit) {
			lastW = window.width
			lastH = window.height
			while (true) {
				particles.forEach { it.update() }
				if (lastW != window.width || lastH != window.height) {
					particles = List(80) { TitleParticle() }
					lastW = window.width
					lastH = window.height
				}
				kotlinx.coroutines.delay(16)
			}
		}

		val background = remember {
			runCatching {
				javaClass.getResourceAsStream("/assets/naitool/ui/background.jpg")
					?.use { it.readBytes().decodeToImageBitmap() }
			}.getOrNull()
		}

		Box(Modifier.fillMaxSize().background(Color(23, 8, 20))) {
			if (background != null) {
				Image(
					bitmap = background,
					contentDescription = null,
					modifier = Modifier.fillMaxSize(),
					contentScale = ContentScale.Crop
				)
			}
			// 压暗一层，突出前景
			Box(Modifier.fillMaxSize().background(Color(0f, 0f, 0f, 0.35f)))

			Canvas(modifier = Modifier.fillMaxSize()) {
				particles.forEach { p ->
					drawCircle(Color.White.copy(alpha = 0.8f), 3f, Offset(p.x, p.y))
				}
				val linkDist = 160f
				for (i in particles.indices) {
					val a = particles[i]
					for (j in i + 1 until particles.size) {
						val b = particles[j]
						val dx = a.x - b.x
						val dy = a.y - b.y
						val dist = sqrt(dx * dx + dy * dy)
						if (dist < linkDist) {
							drawLine(
								Color.White.copy(alpha = 1f - dist / linkDist),
								Offset(a.x, a.y),
								Offset(b.x, b.y),
								strokeWidth = 1f
							)
						}
					}
				}
			}

			AnimatedVisibility(
				visible = visible,
				enter = fadeIn(tween(500)) + scaleIn(tween(500, easing = FastOutSlowInEasing)) +
						expandVertically(tween(500)),
				exit = fadeOut() + scaleOut(),
				modifier = Modifier.align(Alignment.Center)
			) {
				Column(horizontalAlignment = Alignment.CenterHorizontally) {
					Text(
						"NaiTool",
						fontSize = 24.sp,
						color = Color.White,
						style = TextStyle(
							shadow = Shadow(Color(108, 53, 222), Offset(1f, 1f), 6f)
						)
					)
					Text(
						"26.1.2",
						fontSize = 7.sp,
						color = Color(170, 150, 255),
						modifier = Modifier.padding(top = 2.dp)
					)

					Column(
						modifier = Modifier
							.padding(top = 14.dp)
							.fillMaxWidth(),
						horizontalAlignment = Alignment.CenterHorizontally
					) {
						MenuButton(StringUtils.translate("naitool.ui.title.singleplayer")) {
							Minecraft.getInstance().setScreen(SelectWorldScreen(this@NaiTitleScreen))
						}
						MenuButton(StringUtils.translate("naitool.ui.title.multiplayer")) {
							Minecraft.getInstance().setScreen(JoinMultiplayerScreen(this@NaiTitleScreen))
						}
						MenuButton(StringUtils.translate("naitool.ui.title.options")) {
							Minecraft.getInstance().setScreen(
								OptionsScreen(this@NaiTitleScreen, Minecraft.getInstance().options, false)
							)
						}
						MenuButton(StringUtils.translate("naitool.ui.title.modSettings")) {
							Minecraft.getInstance().setScreen(ClickGuiScreen(this@NaiTitleScreen))
						}
						MenuButton(StringUtils.translate("naitool.ui.title.quit")) {
							Minecraft.getInstance().stop()
						}
					}
				}
			}
		}
	}
}

class TitleParticle {
	private val w get() = Minecraft.getInstance().window.width.toFloat()
	private val h get() = Minecraft.getInstance().window.height.toFloat()

	var x by mutableStateOf(Random.nextFloat() * w)
	var y by mutableStateOf(Random.nextFloat() * h)
	private val speed = 0.6f
	private var vX = (Random.nextFloat() * 2 - 1) * speed
	private var vY = (Random.nextFloat() * 2 - 1) * speed

	fun update() {
		if (x >= w || x <= 0f) vX *= -1
		if (y >= h || y <= 0f) vY *= -1
		x += vX
		y += vY
	}
}

@Composable
private fun MenuButton(text: String, onClick: () -> Unit) {
	Surface(
		modifier = Modifier
			.padding(vertical = 3.dp)
			.width(130.dp)
			.height(22.dp)
			.clickable(onClick = onClick),
		color = Color.Black.copy(alpha = 0.55f),
		shape = RoundedCornerShape(6.dp)
	) {
		Box(contentAlignment = Alignment.Center) {
			Text(text, color = Color.White, fontSize = 8.sp)
		}
	}
}
