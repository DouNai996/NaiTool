package com.naitool.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.naitool.feature.GhostMine;
import com.naitool.feature.NightVision;
import com.naitool.feature.NoFall;
import com.naitool.feature.Sprint;
import net.minecraft.client.Minecraft;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
    @Inject(at = @At("HEAD"), method = "tick")
    private void naitool$onPreTick(CallbackInfo ci) {
        NoFall.clientPreTick();
    }

    @Inject(at = @At("TAIL"), method = "tick")
    private void naitool$onPostTick(CallbackInfo ci) {
        NightVision.tick();
        Sprint.tick();
        GhostMine.tick();
    }
}
