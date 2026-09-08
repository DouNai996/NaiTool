package com.naitool.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.naitool.feature.ElytraTrails;
import net.minecraft.client.player.LocalPlayer;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerTickMixin {
    @Inject(at = @At("TAIL"), method = "tick")
    private void naitool$onTickTail(CallbackInfo ci) {
        ElytraTrails.tick();
    }
}