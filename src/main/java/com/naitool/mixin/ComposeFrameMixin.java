package com.naitool.mixin;

import com.naitool.ui.core.ComposeRenderer;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 26.1.2 渲染管线为保留式提取：GameRenderer#render 只画到主 RenderTarget，
 * renderFrame 中先 mainRenderTarget.blitToScreen() 输出到默认帧缓冲，
 * 随后 RenderSystem.flipFrame() 交换缓冲区。
 * 必须夹在 blitToScreen 之后、flipFrame 之前绘制：
 * TAIL 已晚于 swap，画到的是下一帧才会被覆盖的后缓冲。
 */
@Mixin(Minecraft.class)
public class ComposeFrameMixin {
	@Inject(
		method = "renderFrame(Z)V",
		at = @At(
			value = "INVOKE",
			target = "Lcom/mojang/blaze3d/systems/RenderSystem;flipFrame(Lcom/mojang/blaze3d/TracyFrameCapture;)V",
			shift = At.Shift.BEFORE
		)
	)
	private void naitool$onBeforeSwap(boolean tick, CallbackInfo ci) {
		ComposeRenderer.INSTANCE.onFrameRendered();
	}
}
