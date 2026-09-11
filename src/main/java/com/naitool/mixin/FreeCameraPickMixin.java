package com.naitool.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.naitool.feature.FreeCameraInteractions;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

@Mixin(Minecraft.class)
public abstract class FreeCameraPickMixin {
    @Inject(method = "pick", at = @At("TAIL"))
    private void naitool$useFreeCameraCrosshair(float tickDelta, CallbackInfo ci) {
        Minecraft client = (Minecraft) (Object) this;
        if (!FreeCameraInteractions.shouldOverrideCrosshair(client)) {
            return;
        }

        Entity camera = client.getCameraEntity();
        HitResult target = client.player.raycastHitResult(tickDelta, camera);
        target = FreeCameraInteractions.filterCrosshairTarget(client, camera, target);
        client.hitResult = target;
        client.crosshairPickEntity = target instanceof EntityHitResult entityHitResult
                ? entityHitResult.getEntity()
                : null;
    }
}