package com.naitool.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.naitool.feature.FreeCameraInteractions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;

@Mixin(MultiPlayerGameMode.class)
public class FreeCameraInteractionManagerMixin {
    @Inject(method = "useItemOn", at = @At("HEAD"), cancellable = true)
    private void naitool$rejectOutOfRangeBlockUse(
            LocalPlayer player,
            InteractionHand hand,
            BlockHitResult hitResult,
            CallbackInfoReturnable<InteractionResult> cir
    ) {
        if (FreeCameraInteractions.isBlockOutsideServerInteractionRange(
                Minecraft.getInstance(),
                hitResult.getBlockPos()
        )) {
            cir.setReturnValue(InteractionResult.FAIL);
        }
    }

    @Inject(method = "startDestroyBlock", at = @At("HEAD"), cancellable = true)
    private void naitool$rejectOutOfRangeBlockAttack(
            BlockPos position,
            Direction direction,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (FreeCameraInteractions.isBlockOutsideServerInteractionRange(
                Minecraft.getInstance(),
                position
        )) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "continueDestroyBlock", at = @At("HEAD"), cancellable = true)
    private void naitool$rejectOutOfRangeBlockBreaking(
            BlockPos position,
            Direction direction,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (FreeCameraInteractions.isBlockOutsideServerInteractionRange(
                Minecraft.getInstance(),
                position
        )) {
            ((MultiPlayerGameMode) (Object) this).stopDestroyBlock();
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "interact", at = @At("HEAD"), cancellable = true)
    private void naitool$rejectOutOfRangeEntityUseAtLocation(
            Player player,
            Entity entity,
            EntityHitResult hitResult,
            InteractionHand hand,
            CallbackInfoReturnable<InteractionResult> cir
    ) {
        if (FreeCameraInteractions.isEntityOutsideServerInteractionRange(
                Minecraft.getInstance(),
                entity
        )) {
            cir.setReturnValue(InteractionResult.FAIL);
        }
    }

    @Inject(method = "attack", at = @At("HEAD"), cancellable = true)
    private void naitool$rejectOutOfRangeEntityAttack(
            Player player,
            Entity entity,
            CallbackInfo ci
    ) {
        if (FreeCameraInteractions.isEntityOutsideServerInteractionRange(
                Minecraft.getInstance(),
                entity
        )) {
            ci.cancel();
        }
    }
}