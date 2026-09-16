package com.naitool.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.gen.Invoker;

import com.naitool.feature.GhostMine;
import com.naitool.mixininterface.IMultiPlayerGameMode;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.multiplayer.prediction.PredictiveAction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

@Mixin(MultiPlayerGameMode.class)
public abstract class GhostMineGameModeMixin implements IMultiPlayerGameMode {
    @Shadow
    protected abstract void ensureHasSentCarriedItem();

    @Override
    public void naitool$syncSelected() {
        ensureHasSentCarriedItem();
    }

    @Invoker("startPrediction")
    @Override
    public abstract void naitool$startPrediction(ClientLevel level, PredictiveAction action);

    @Inject(method = "startDestroyBlock", at = @At("HEAD"), cancellable = true)
    private void naitool$onStartDestroyBlock(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        if (GhostMine.onPlayerAttack(pos, direction)) {
            cir.setReturnValue(false);
        }
    }
}
