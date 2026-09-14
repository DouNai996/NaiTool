package com.naitool.feature;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.item.component.Fireworks;

import com.naitool.config.Configs;

public final class ElytraBoost {
    private ElytraBoost() {}

    private static final Set<FireworkRocketEntity> FIREWORKS =
            Collections.newSetFromMap(new IdentityHashMap<>());

    private static boolean bypassing;

    /** 真实发射烟花时，绕过 MultiPlayerGameModeMixin 的防消耗拦截，避免递归与误取消。 */
    public static boolean isBypassing() {
        return bypassing;
    }

    public static void boost() {
        Minecraft mc = Minecraft.getInstance();

        if (mc.player == null || mc.level == null || mc.gui.screen() != null) {
            return;
        }
        if (!mc.player.isFallFlying()) {
            return;
        }
        if (!Configs.Generic.ELYTRA_BOOST_ENABLED.getBooleanValue()) {
            return;
        }

        // 移植自 OnekeyFireWork：使用真实烟花（服务端有效，会消耗烟花）
        if (Configs.Generic.ELYTRA_BOOST_REAL_FIREWORK.getBooleanValue()) {
            realFirework(mc);
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

    /**
     * 按 副手 → 当前手持 → 快捷栏 → 背包 的顺序寻找烟花并真正使用。
     * 快捷栏外的烟花会临时交换到当前选中槽位，发射后再换回原位。
     */
    private static void realFirework(Minecraft mc) {
        if (mc.gameMode == null || mc.getConnection() == null || mc.player.containerMenu == null) {
            return;
        }

        if (mc.player.getOffhandItem().is(Items.FIREWORK_ROCKET)) {
            useRealItem(mc, InteractionHand.OFF_HAND);
            return;
        }

        if (mc.player.getMainHandItem().is(Items.FIREWORK_ROCKET)) {
            useRealItem(mc, InteractionHand.MAIN_HAND);
            return;
        }

        int selected = mc.player.getInventory().getSelectedSlot();

        // 快捷栏内查找：切换过去发射再切回
        for (int slot = 0; slot < 9; slot++) {
            if (slot == selected) {
                continue;
            }
            if (mc.player.getInventory().getItem(slot).is(Items.FIREWORK_ROCKET)) {
                selectSlot(mc, slot);
                useRealItem(mc, InteractionHand.MAIN_HAND);
                selectSlot(mc, selected);
                return;
            }
        }

        // 背包（主物品栏 9~35）内查找：临时交换到当前快捷栏，发射后再换回原位
        for (int slot = 9; slot <= 35; slot++) {
            if (mc.player.getInventory().getItem(slot).is(Items.FIREWORK_ROCKET)) {
                swapWithHotbar(mc, slot, selected);
                useRealItem(mc, InteractionHand.MAIN_HAND);
                swapWithHotbar(mc, slot, selected);
                return;
            }
        }
    }

    /** 将背包索引（Inventory 下标）映射为玩家背包菜单 InventoryMenu 的槽位 id。 */
    private static int playerSlotId(int invIndex) {
        if (invIndex >= 0 && invIndex <= 8) return 36 + invIndex;
        if (invIndex >= 9 && invIndex <= 35) return invIndex;
        if (invIndex == 40) return 45;
        return -1;
    }

    private static void clickSlot(Minecraft mc, int slotId) {
        mc.gameMode.handleContainerInput(
                mc.player.containerMenu.containerId, slotId, 0, ContainerInput.PICKUP, mc.player);
    }

    /** 用原版“拾起-放下-归位”点击交换背包槽与快捷栏槽的整组物品，可重复调用实现换回。 */
    private static void swapWithHotbar(Minecraft mc, int invIndex, int hotbarIndex) {
        int fromId = playerSlotId(invIndex);
        int toId = playerSlotId(hotbarIndex);
        if (fromId < 0 || toId < 0) {
            return;
        }

        boolean hadEmptyCursor = mc.player.containerMenu.getCarried().isEmpty();
        clickSlot(mc, fromId);
        clickSlot(mc, toId);
        if (hadEmptyCursor && !mc.player.containerMenu.getCarried().isEmpty()) {
            clickSlot(mc, fromId);
        }
    }

    private static void useRealItem(Minecraft mc, InteractionHand hand) {
        bypassing = true;
        try {
            mc.gameMode.useItem(mc.player, hand);
            mc.player.swing(hand);
        } finally {
            bypassing = false;
        }
    }

    private static void selectSlot(Minecraft mc, int slot) {
        mc.player.getInventory().setSelectedSlot(slot);
        mc.getConnection().send(new ServerboundSetCarriedItemPacket(slot));
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
