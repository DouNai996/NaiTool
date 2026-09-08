package com.naitool.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.naitool.feature.ElytraTrails;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;

@Mixin(ClientLevel.class)
public abstract class ClientLevelParticleMixin {
    @Inject(method = "addParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)V",
            at = @At("HEAD"), cancellable = true)
    private void naitool$filterFireworkParticles(ParticleOptions particleOptions,
                                                  double x, double y, double z,
                                                  double vx, double vy, double vz,
                                                  CallbackInfo ci) {
        if (ElytraTrails.shouldHideFireworks()
                && ElytraTrails.isPlayerGliding()
                && particleOptions.getType() == ParticleTypes.FIREWORK) {
            ci.cancel();
        }
    }
}