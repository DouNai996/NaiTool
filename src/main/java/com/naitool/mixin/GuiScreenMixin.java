package com.naitool.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.naitool.event.ScreenTracker;

@Mixin(Minecraft.class)
public abstract class GuiScreenMixin {
    @Inject(method = "setScreen", at = @At("HEAD"))
    private void naitool$onSetScreen(Screen screen, CallbackInfo ci) {
        ScreenTracker.setCurrentScreen(screen);
    }
}
