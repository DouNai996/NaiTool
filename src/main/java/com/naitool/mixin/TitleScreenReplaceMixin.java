package com.naitool.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.naitool.config.Configs;
import com.naitool.ui.title.NaiTitleScreen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;

/**
 * 当游戏准备显示原版标题屏时，替换为 NaiTool 自定义标题屏。
 * 受 interfaceCustomTitle 开关控制；NaiTitleScreen 自身不触发替换，避免递归。
 */
@Mixin(Minecraft.class)
public class TitleScreenReplaceMixin {
    @Inject(method = "setScreen", at = @At("HEAD"), cancellable = true)
    private void naitool$replaceTitleScreen(Screen screen, CallbackInfo ci) {
        if (screen != null
                && screen.getClass() == TitleScreen.class
                && Configs.Generic.INTERFACE_CUSTOM_TITLE.getBooleanValue()) {
            ci.cancel();
            ((Minecraft) (Object) this).setScreen(new NaiTitleScreen());
        }
    }
}
