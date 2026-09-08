package com.naitool.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.naitool.config.Configs;
import com.naitool.feature.ElytraBoost;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

@Mixin(MultiPlayerGameMode.class)
public abstract class MultiPlayerGameModeMixin {
    @Inject(method = "useItem", at = @At("HEAD"), cancellable = true)
    private void naitool$onUseItem(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        Minecraft mc = Minecraft.getInstance();

        if (mc.player == null || mc.player != player) return;
        if (!Configs.Generic.ELYTRA_BOOST_ENABLED.getBooleanValue()) return;
        if (!Configs.Generic.ELYTRA_BOOST_DONT_CONSUME.getBooleanValue()) return;
        if (!player.isFallFlying() || mc.gui.screen() != null) return;

        ItemStack stack = player.getItemInHand(hand);
        if (stack.is(Items.FIREWORK_ROCKET)) {
            ElytraBoost.boost();
            cir.setReturnValue(InteractionResult.PASS);
        }
    }
}
