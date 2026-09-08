package com.naitool.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.naitool.feature.Sprint;
import net.minecraft.client.player.LocalPlayer;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin {
    @Inject(at = @At("TAIL"), method = "aiStep")
    private void naitool$onAiStep(CallbackInfo ci) {
        LocalPlayer self = (LocalPlayer) (Object) this;

        if (!Sprint.isEnabled()) {
            return;
        }
        if (self.isInWater() || self.isMobilityRestricted()) {
            return;
        }

        Sprint.Mode mode = Sprint.getActiveMode();

        if (mode == Sprint.Mode.STRICT) {
            if (self.zza > 0.8F && self.getFoodData().hasEnoughFood()) {
                self.setSprinting(true);
            }
        } else {
            float movement = Math.abs(self.zza) + Math.abs(self.xxa);
            if (movement > (self.isUnderWater() ? 1.0E-5F : 0.8F)) {
                self.setSprinting(true);
            }
        }
    }
}
