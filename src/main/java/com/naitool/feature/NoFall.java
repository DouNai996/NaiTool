package com.naitool.feature;

import com.naitool.config.Configs;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;

/**
 * 无摔伤
 */
public final class NoFall {
    private NoFall() {}

    private static boolean lastEnabled = false;

    public static boolean isEnabled() {
        return Configs.Generic.NO_FALL_ENABLED.getBooleanValue();
    }

    /** 客户端 tick 起始（等价 TickEvent.Pre）。 */
    public static void clientPreTick() {
        boolean enabled = isEnabled();
        if (enabled != lastEnabled) {
            lastEnabled = enabled;
            if (enabled) {
                onActivate();
            }
        }
        if (!enabled) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            return;
        }

        handleViaFabricMode(mc);
    }

    private static void onActivate() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }
    }

    private static void handleViaFabricMode(Minecraft mc) {
        if (!isFalling(mc)) {
            return;
        }

        mc.getConnection().send(new ServerboundMovePlayerPacket.PosRot(
                mc.player.getX(),
                mc.player.getY() + 0.000000001,
                mc.player.getZ(),
                mc.player.getYRot(),
                mc.player.getXRot(),
                false,
                mc.player.horizontalCollision));
        mc.player.resetFallDistance();
    }

    private static boolean isFalling(Minecraft mc) {
        return mc.player.fallDistance > mc.player.getMaxFallDistance()
                && !mc.player.onGround()
                && !mc.player.isFallFlying();
    }
}
