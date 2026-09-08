package com.naitool.feature;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.item.component.Fireworks;

import com.naitool.config.Configs;

public final class ElytraBoost {
    private ElytraBoost() {}

    private static final Set<FireworkRocketEntity> FIREWORKS =
            Collections.newSetFromMap(new IdentityHashMap<>());

    public static void boost() {
        Minecraft mc = Minecraft.getInstance();

        if (mc.player == null || mc.level == null || mc.gui.screen() != null) {
            return;
        }
        if (!mc.player.isFallFlying()) {
            return;
        }

        FIREWORKS.removeIf(Entity::isRemoved);

        boolean dontConsume = Configs.Generic.ELYTRA_BOOST_DONT_CONSUME.getBooleanValue();

        ItemStack held = ItemStack.EMPTY;
        if (!dontConsume) {
            held = findFirework(mc);
            if (held.isEmpty()) {
                return;
            }
        }

        ItemStack firework = Items.FIREWORK_ROCKET.getDefaultInstance();
        Fireworks original = firework.get(DataComponents.FIREWORKS);
        List<FireworkExplosion> explosions = original != null ? original.explosions() : List.of();
        int level = Configs.Generic.ELYTRA_BOOST_FIREWORK_LEVEL.getIntegerValue();
        firework.set(DataComponents.FIREWORKS, new Fireworks(level, explosions));

        FireworkRocketEntity entity = new FireworkRocketEntity(mc.level, firework, mc.player);
        FIREWORKS.add(entity);

        if (Configs.Generic.ELYTRA_BOOST_PLAY_SOUND.getBooleanValue()) {
            mc.level.playSound(mc.player, entity, SoundEvents.FIREWORK_ROCKET_LAUNCH, SoundSource.AMBIENT, 3.0F, 1.0F);
        }

        mc.level.addEntity(entity);

        if (!dontConsume) {
            held.shrink(1);
        }
    }

    public static boolean isFirework(FireworkRocketEntity firework) {
        return FIREWORKS.contains(firework);
    }

    private static ItemStack findFirework(Minecraft mc) {
        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack stack = mc.player.getItemInHand(hand);
            if (stack.is(Items.FIREWORK_ROCKET)) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }
}
