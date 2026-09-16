package com.naitool.feature;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.naitool.config.Configs;
import com.naitool.mixininterface.IMultiPlayerGameMode;
import com.naitool.render.Color;
import com.naitool.render.Renderer3D;
import com.naitool.render.ShapeMode;

import fi.dy.masa.malilib.config.IConfigOptionListEntry;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * GhostMine 发包挖掘 - 从 Meteor addon 移植到 NaiTool(MaLiLib + Mixin, MC 26.1.2 Mojmap)。
 *
 * 核心机制与原版一致：
 * 1. 进度模拟：使用真实破坏速度公式模拟原版挖掘时间
 * 2. 发包顺序：START -> 等待进度 -> STOP(序列包)
 * 3. 绕过：高空包、滞空挖掘微调
 * 4. 工具切换：进度达到阈值自动切换最佳工具
 * 5. 失败保护：连续完成但未破坏达到上限则放弃
 */
public final class GhostMine {
    private GhostMine() {}

    private static boolean lastEnabled = false;

    private static BlockDate firstBlockDate = null;
    private static BlockDate rebreakBlockDate = null;
    private static final List<BlockDate> queue = new ArrayList<>();

    private static int rebreakTicks = 0;
    private static int breakAttempts = 0;
    private static long lastMineTime = 0;

    private static int previousSlot = -1;
    private static boolean hasSwitch = false;

    private static final List<net.minecraft.world.level.block.Block> UNBREAKABLE = Arrays.asList(
            Blocks.COMMAND_BLOCK,
            Blocks.LAVA_CAULDRON,
            Blocks.LAVA,
            Blocks.WATER_CAULDRON,
            Blocks.WATER,
            Blocks.BEDROCK,
            Blocks.BARRIER,
            Blocks.END_PORTAL,
            Blocks.NETHER_PORTAL,
            Blocks.END_PORTAL_FRAME
    );

    public static boolean isEnabled() {
        return Configs.Generic.GHOST_MINE_ENABLED.getBooleanValue();
    }

    private static double speed() {
        return Configs.Generic.GHOST_MINE_SPEED.getDoubleValue();
    }

    private static int range() {
        return Configs.Generic.GHOST_MINE_RANGE.getIntegerValue();
    }

    // ==================== 生命周期 ====================

    public static void tick() {
        Minecraft mc = Minecraft.getInstance();
        boolean enabled = isEnabled();

        if (enabled && !lastEnabled) {
            onActivate(mc);
        } else if (!enabled && lastEnabled) {
            onDeactivate(mc);
        }
        lastEnabled = enabled;

        if (!enabled || mc.player == null || mc.level == null || mc.getConnection() == null) {
            return;
        }

        rangeCheck(mc);
        rebreakTicks++;
        tickMining(mc);
    }

    private static void onActivate(Minecraft mc) {
        firstBlockDate = null;
        rebreakBlockDate = null;
        rebreakTicks = 0;
        breakAttempts = 0;
        lastMineTime = 0;
        previousSlot = -1;
        hasSwitch = false;
        queue.clear();
    }

    private static void onDeactivate(Minecraft mc) {
        firstBlockDate = null;
        rebreakBlockDate = null;
        rebreakTicks = 0;
        breakAttempts = 0;
        restoreSwap(mc);
        queue.clear();
    }

    // ==================== 攻击入口(替代 StartBreakingBlockEvent) ====================

    /**
     * @return true 表示已接管、需要取消原版挖掘
     */
    public static boolean onPlayerAttack(BlockPos pos, Direction direction) {
        Minecraft mc = Minecraft.getInstance();
        if (!isEnabled() || mc.player == null || mc.level == null) {
            return false;
        }

        BlockState state = mc.level.getBlockState(pos);
        if (UNBREAKABLE.contains(state.getBlock()) || distanceTo(mc, pos) > range()) {
            return false;
        }

        // 当前目标已存在，忽略重复
        if (firstBlockDate != null && pos.equals(firstBlockDate.pos)) {
            return true;
        }
        // 队列去重
        for (BlockDate q : queue) {
            if (pos.equals(q.pos)) {
                return true;
            }
        }
        if (queue.size() < 256) {
            queue.add(new BlockDate(pos, direction));
        }
        return true; // 取消原版挖掘，统一由队列驱动
    }

    // ==================== 挖掘主循环 ====================

    private static void tickMining(Minecraft mc) {
        if (firstBlockDate != null && mc.level.getBlockState(firstBlockDate.pos).isAir()) {
            firstBlockDate = null;
        }

        if (firstBlockDate == null) {
            firstBlockDate = pollQueue();
        }

        if (firstBlockDate != null && !firstBlockDate.isMining) {
            mineBlock(mc, firstBlockDate.pos, firstBlockDate.direction);
            firstBlockDate.isMining = true;
            lastMineTime = System.currentTimeMillis();
        }

        if (firstBlockDate != null && firstBlockDate.isMining && !firstBlockDate.done) {
            firstBlockDate.freshProgress(mc);
        }

        if (firstBlockDate != null && firstBlockDate.isMining && firstBlockDate.done) {
            sendStop(mc, firstBlockDate.pos, firstBlockDate.direction);
            rebreakBlockDate = firstBlockDate.rebreak ? new BlockDate(firstBlockDate.pos, firstBlockDate.direction) : null;
            firstBlockDate = null;
            breakAttempts = 0;
        }

        handleBreakAttempts();
        handleToolSwitch(mc, firstBlockDate);
        handleRebreak(mc);
    }

    private static void handleBreakAttempts() {
        if (firstBlockDate == null || !firstBlockDate.done) {
            return;
        }
        breakAttempts++;
        if (breakAttempts >= Configs.Generic.GHOST_MINE_MAX_BREAKS.getIntegerValue() * 10) {
            firstBlockDate = null;
            rebreakBlockDate = null;
            breakAttempts = 0;
        }
    }

    private static void handleToolSwitch(Minecraft mc, BlockDate blockDate) {
        if (blockDate == null || !blockDate.isMining || blockDate.done) {
            return;
        }
        double progressPercent = blockDate.progress * (1.0 / speed()) * 100;
        if (progressPercent >= Configs.Generic.GHOST_MINE_SWITCH_DAMAGE.getIntegerValue() && !hasSwitch) {
            int bestSlot = getBestTool(mc, mc.level.getBlockState(blockDate.pos));
            if (bestSlot != -1 && bestSlot != mc.player.getInventory().getSelectedSlot()) {
                swapTo(mc, bestSlot, true);
                hasSwitch = true;
                lastMineTime = System.currentTimeMillis();
            }
        }
        if (hasSwitch && System.currentTimeMillis() - lastMineTime > Configs.Generic.GHOST_MINE_SWITCH_TIME.getIntegerValue()) {
            restoreSwap(mc);
            hasSwitch = false;
        }
    }

    private static void handleRebreak(Minecraft mc) {
        if (rebreakBlockDate == null || firstBlockDate != null) {
            return;
        }
        if (!Configs.Generic.GHOST_MINE_REBREAK.getBooleanValue()
                || rebreakTicks < Configs.Generic.GHOST_MINE_REBREAK_DELAY.getIntegerValue() * 4) {
            return;
        }

        BlockState state = mc.level.getBlockState(rebreakBlockDate.pos);
        if (state.isAir() || state.is(Blocks.WATER) || state.is(Blocks.LAVA)) {
            return;
        }

        int slot = getBestTool(mc, state);
        if (slot != -1 && slot != mc.player.getInventory().getSelectedSlot()) {
            swapTo(mc, slot, false);
        }

        mc.getConnection().send(new ServerboundPlayerActionPacket(
                ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK,
                rebreakBlockDate.pos, rebreakBlockDate.direction));
        rebreakTicks = 0;
    }

    public static void rangeCheck(Minecraft mc) {
        if (firstBlockDate != null && distanceTo(mc, firstBlockDate.pos) > range()) {
            firstBlockDate = null;
        }
        if (rebreakBlockDate != null && distanceTo(mc, rebreakBlockDate.pos) > range()) {
            rebreakBlockDate = null;
        }
    }

    /**
     * 从待挖队列取出下一个有效目标（跳过已破坏/超距/不可破坏方块）
     */
    private static BlockDate pollQueue() {
        Minecraft mc = Minecraft.getInstance();
        while (!queue.isEmpty()) {
            BlockDate bd = queue.remove(0);
            if (mc.level == null || mc.player == null) {
                break;
            }
            BlockState st = mc.level.getBlockState(bd.pos);
            if (st.isAir() || UNBREAKABLE.contains(st.getBlock()) || distanceTo(mc, bd.pos) > range()) {
                continue;
            }
            return bd;
        }
        return null;
    }

    // ==================== 渲染 ====================
    // 固化配色：待挖=青，挖掘中=粉，就绪=金

    private static final Color QUEUE_SIDE = new Color(255, 0, 0, 90);
    private static final Color QUEUE_LINE = new Color(255, 0, 0, 235);
    private static final Color MINE_SIDE = new Color(255, 140, 185, 70);
    private static final Color MINE_LINE = new Color(255, 110, 170, 235);
    private static final Color READY_SIDE = new Color(255, 215, 105, 80);
    private static final Color READY_LINE = new Color(255, 225, 130, 255);

    public static void draw(Renderer3D r, Minecraft mc) {
        if (mc.level == null) {
            return;
        }

        ShapeMode mode = currentShapeMode();
        ShapeMode queueMode = currentQueueShapeMode();
        double sp = speed();
        long nowMs = System.currentTimeMillis();
        double now = nowMs / 1000.0;

        // ===== 待挖掘队列 =====
        if (Configs.Generic.GHOST_MINE_QUEUE_RENDER.getBooleanValue()) {
            for (int i = 0; i < queue.size(); i++) {
                BlockDate bd = queue.get(i);
                if (!mc.level.getBlockState(bd.pos).isAir()) {
                    renderQueued(r, bd.pos, i, bd.addedAt, nowMs, now, queueMode);
                }
            }
        }

        // ===== 挖掘中 =====
        if (firstBlockDate != null && !mc.level.getBlockState(firstBlockDate.pos).isAir()) {
            renderMining(r, firstBlockDate.pos, firstBlockDate.progress, sp, mode, now);
        }

        // ===== 等待重挖 =====
        if (Configs.Generic.GHOST_MINE_REBREAK.getBooleanValue()
                && rebreakBlockDate != null && firstBlockDate == null) {
            BlockState st = mc.level.getBlockState(rebreakBlockDate.pos);
            if (!st.isAir() && !st.is(Blocks.WATER) && !st.is(Blocks.LAVA)) {
                BlockPos p = rebreakBlockDate.pos;
                r.box(p.getX(), p.getY(), p.getZ(), p.getX() + 1, p.getY() + 1, p.getZ() + 1,
                        READY_SIDE, READY_LINE, mode);
                if (mode.lines()) {
                    // 声呐式扩散 ping 环
                    for (int k = 0; k < 3; k++) {
                        double f = (now * 0.75 + k / 3.0) % 1.0;
                        double e = 0.14 * f;
                        int a = (int) (READY_LINE.a * (1.0 - f) * 0.8);
                        if (a > 0) {
                            r.boxLines(p.getX() - e, p.getY() - e, p.getZ() - e,
                                    p.getX() + 1 + e, p.getY() + 1 + e, p.getZ() + 1 + e,
                                    READY_LINE.withAlpha(a));
                        }
                    }
                }
            }
        }
    }

    /** 待挖掘方块：入列弹跳 + 呼吸填充 + 行军虚线（队首为能量流光）。 */
    private static void renderQueued(Renderer3D r, BlockPos p, int index, long addedAt,
                                     long nowMs, double now, ShapeMode mode) {
        double age = nowMs - addedAt;
        double scale = 1.0;
        if (age < 220.0) {
            double t = age / 220.0;
            double ease = 1.0 - Math.pow(1.0 - t, 3.0); // easeOutCubic
            scale = 0.9 + 0.1 * ease;
        }
        double pad = (1.0 - scale) / 2.0;
        double x1 = p.getX() + pad;
        double y1 = p.getY() + pad;
        double z1 = p.getZ() + pad;
        double x2 = p.getX() + 1.0 - pad;
        double y2 = p.getY() + 1.0 - pad;
        double z2 = p.getZ() + 1.0 - pad;

        boolean head = index == 0;
        double breath = 0.75 + 0.25 * Math.sin(now * 3.0 - index * 0.35);
        double brightness = head ? 1.0 : 0.6;

        if (mode.sides()) {
            int a = (int) (QUEUE_SIDE.a * 0.35 * brightness * breath);
            r.boxSides(x1, y1, z1, x2, y2, z2, QUEUE_SIDE.withAlpha(a));
        }

        if (mode.lines()) {
            int alpha = (int) (QUEUE_LINE.a * (head ? 0.95 : 0.7) * breath);
            Color base = QUEUE_LINE.withAlpha(alpha);
            if (head) {
                // 队首（下一个目标）：能量流光
                r.boxLinesFlow(x1, y1, z1, x2, y2, z2, base, now * 0.9, 8);
            } else {
                // 其余队列：蚂蚁行军虚线，按队列顺序错相形成波浪
                Color off = base.mulRGB(0.15).withAlpha((int) (alpha * 0.25));
                double shift = -now * 5.0 + (index % 4);
                r.boxLinesMarching(x1, y1, z1, x2, y2, z2, base, off, shift, 8);
            }
        }
    }

    /**
     * 挖掘中方块（现代动画）：
     * 进度框从完整方块向中心收缩（进度 0 最大，1 消失）+ 沿棱流光
     * + 8 角战术括角脉冲 + 上下双向旋转能量环 + 排空式进度面 + 上下扫描环 + 就绪闪烁。
     */
    private static void renderMining(Renderer3D r, BlockPos p, double rawProgress, double speed,
                                     ShapeMode mode, double now) {
        double progress = Math.max(0.0, Math.min(1.0, rawProgress * (1.0 / speed)));

        double bx = p.getX(), by = p.getY(), bz = p.getZ();
        double cx = bx + 0.5, cy = by + 0.5, cz = bz + 0.5;

        // 进度框：从大到小（满进度时收缩到中心）
        double half = 0.5 * (1.0 - progress);
        double x1 = cx - half, y1 = cy - half, z1 = cz - half;
        double x2 = cx + half, y2 = cy + half, z2 = cz + half;

        Color s = Color.lerp(MINE_SIDE, READY_SIDE, progress);
        Color l = Color.lerp(MINE_LINE, READY_LINE, progress);

        // 就绪时快速闪烁
        if (progress >= 1.0) {
            double blink = 0.6 + 0.4 * Math.sin(now * 12.0);
            s = s.mulAll(blink);
            l = l.mulAll(blink);
        }

        if (mode.sides()) {
            r.boxSides(x1, y1, z1, x2, y2, z2, s);
        }
        if (mode.lines()) {
            r.boxLinesFlow(x1, y1, z1, x2, y2, z2, l, now * 0.7, 8);
        }

        if (progress >= 1.0) {
            return;
        }

        double pulse = 0.72 + 0.28 * Math.sin(now * 4.5);

        // 8 角战术括角：固定在完整方块边界，随进度向就绪色过渡并脉冲
        if (mode.lines()) {
            Color bracket = Color.lerp(MINE_LINE, READY_LINE, progress)
                    .withAlpha((int) (235 * pulse));
            r.cornerBrackets(bx, by, bz, bx + 1.0, by + 1.0, bz + 1.0, 0.17, bracket);

            // 上下两层反向旋转能量环（加载环）
            Color spinA = READY_LINE.withAlpha((int) (200 * pulse));
            Color spinB = MINE_LINE.withAlpha((int) (140 * pulse));
            r.rotatingRing(cx, by + 1.04, cz, 0.66, now * 2.2, spinA);
            r.rotatingRing(cx, by - 0.04, cz, 0.66, -now * 1.6 + Math.PI / 4.0, spinB);
        }

        // 排空式进度面：从方块顶部随进度向下降
        if (mode.sides()) {
            double fy = by + 1.0 - progress * 0.94;
            r.planeAtY(bx, bz, bx + 1.0, bz + 1.0, fy,
                    READY_SIDE.withAlpha((int) (55 * pulse)));
        }

        // 上下循环扫描环 / 扫描面
        double f = (now * 0.8) % 1.0;
        double fade = Math.sin(f * Math.PI);
        double y = by + 0.06 + f * 0.88;
        if (mode.lines()) {
            r.ringAtY(bx, bz, bx + 1.0, bz + 1.0, y, 0.03, l.withAlpha((int) (210 * fade)));
        }
        if (mode.sides()) {
            r.planeAtY(bx, bz, bx + 1.0, bz + 1.0, y, READY_SIDE.withAlpha((int) (40 * fade)));
        }
    }

    private static ShapeMode currentShapeMode() {
        return ((ShapeModeOption) Configs.Generic.GHOST_MINE_SHAPE_MODE.getOptionListValue()).toShapeMode();
    }

    private static ShapeMode currentQueueShapeMode() {
        return ((ShapeModeOption) Configs.Generic.GHOST_MINE_QUEUE_SHAPE_MODE.getOptionListValue()).toShapeMode();
    }

    public enum ShapeModeOption implements IConfigOptionListEntry {
        LINES("lines", ShapeMode.Lines),
        SIDES("sides", ShapeMode.Sides),
        BOTH("both", ShapeMode.Both);

        private final String stringValue;
        private final ShapeMode shapeMode;

        ShapeModeOption(String stringValue, ShapeMode shapeMode) {
            this.stringValue = stringValue;
            this.shapeMode = shapeMode;
        }

        public ShapeMode toShapeMode() {
            return shapeMode;
        }

        @Override
        public String getStringValue() {
            return stringValue;
        }

        @Override
        public String getDisplayName() {
            return fi.dy.masa.malilib.util.StringUtils.translate(
                    "naitool.config.generic.ghostMineShapeMode." + stringValue);
        }

        @Override
        public IConfigOptionListEntry cycle(boolean forward) {
            int id = ordinal();
            ShapeModeOption[] values = values();
            if (forward) {
                id = (id + 1) % values.length;
            } else {
                id = (id - 1 + values.length) % values.length;
            }
            return values[id];
        }

        @Override
        public IConfigOptionListEntry fromString(String value) {
            for (ShapeModeOption o : values()) {
                if (o.stringValue.equalsIgnoreCase(value)) {
                    return o;
                }
            }
            return BOTH;
        }
    }

    // ==================== 发包 ====================

    private static void mineBlock(Minecraft mc, BlockPos pos, Direction direction) {
        if (Configs.Generic.GHOST_MINE_SWING.getBooleanValue()) {
            mc.player.swing(InteractionHand.MAIN_HAND);
        }

        sendSequenced(mc, ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, pos, direction);

        if (Configs.Generic.GHOST_MINE_FAST_BYPASS.getBooleanValue()) {
            BlockPos bypassPos = new BlockPos(pos.getX(), 321, pos.getZ());
            sendSequenced(mc, ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, bypassPos, Direction.DOWN);
        }
    }

    private static void sendSequenced(Minecraft mc, ServerboundPlayerActionPacket.Action action, BlockPos pos, Direction direction) {
        ((IMultiPlayerGameMode) mc.gameMode).naitool$startPrediction(mc.level,
                sequence -> new ServerboundPlayerActionPacket(action, pos, direction, sequence));
    }

    private static void sendStop(Minecraft mc, BlockPos pos, Direction direction) {
        if (Configs.Generic.GHOST_MINE_BYPASS_GROUND.getBooleanValue()
                && !mc.player.isFallFlying() && pos != null
                && !mc.level.isEmptyBlock(pos) && !mc.player.onGround()) {
            mc.getConnection().send(new ServerboundMovePlayerPacket.PosRot(
                    mc.player.getX(), mc.player.getY() + 1.0e-9, mc.player.getZ(),
                    mc.player.getYRot(), mc.player.getXRot(), true, mc.player.horizontalCollision));
        }

        if (Configs.Generic.GHOST_MINE_FAST_BYPASS.getBooleanValue()) {
            BlockPos bypassPos = new BlockPos(pos.getX(), 321, pos.getZ());
            sendSequenced(mc, ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK, bypassPos, Direction.DOWN);
        }

        sendSequenced(mc, ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK, pos, direction);

        if (Configs.Generic.GHOST_MINE_SWING.getBooleanValue()) {
            mc.player.swing(InteractionHand.MAIN_HAND);
        }
    }

    // ==================== 工具/背包 ====================

    private static void swapTo(Minecraft mc, int slot, boolean remember) {
        if (slot < 0 || slot > 8) {
            return;
        }
        if (remember && previousSlot == -1) {
            previousSlot = mc.player.getInventory().getSelectedSlot();
        } else if (!remember) {
            previousSlot = -1;
        }
        mc.player.getInventory().setSelectedSlot(slot);
        ((IMultiPlayerGameMode) mc.gameMode).naitool$syncSelected();
    }

    private static void restoreSwap(Minecraft mc) {
        if (previousSlot != -1) {
            int slot = previousSlot;
            previousSlot = -1;
            if (mc.player != null) {
                mc.player.getInventory().setSelectedSlot(slot);
                ((IMultiPlayerGameMode) mc.gameMode).naitool$syncSelected();
            }
        }
    }

    private static int getBestTool(Minecraft mc, BlockState state) {
        double bestScore = -1.0;
        int bestSlot = -1;
        for (int i = 0; i < 9; i++) {
            double score = mc.player.getInventory().getItem(i).getDestroySpeed(state);
            if (score > bestScore) {
                bestScore = score;
                bestSlot = i;
            }
        }
        return bestScore > 1.0 ? bestSlot : -1;
    }

    private static double distanceTo(Minecraft mc, BlockPos pos) {
        return mc.player.position().distanceTo(Vec3.atCenterOf(pos));
    }

    private static double getBreakDelta(Minecraft mc, int slot, BlockState state) {
        float hardness = state.getDestroySpeed(null, null);
        if (hardness == -1) {
            return 0;
        }
        ItemStack tool = mc.player.getInventory().getItem(slot);
        double speed = tool.getDestroySpeed(state);
        boolean correctTool = !state.requiresCorrectToolForDrops() || tool.isCorrectToolForDrops(state);
        return speed / hardness / (correctTool ? 30.0F : 100.0F);
    }

    // ==================== 内部类 ====================

    private static final class BlockDate {
        final BlockPos pos;
        final Direction direction;
        final BlockState blockState;
        final long addedAt = System.currentTimeMillis();
        boolean done = false;
        boolean isMining = false;
        boolean rebreak = true;
        double progress = 0.0;

        BlockDate(BlockPos pos, Direction direction) {
            this.pos = pos;
            this.direction = direction;
            this.blockState = Minecraft.getInstance().level.getBlockState(pos);
        }

        void freshProgress(Minecraft mc) {
            float hardness = blockState.getDestroySpeed(mc.level, pos);
            if (hardness == 0) {
                done = true;
                progress = 1.0;
                return;
            }

            int slot = getBestTool(mc, blockState);
            double delta = getBreakDelta(mc, slot != -1 ? slot : mc.player.getInventory().getSelectedSlot(), blockState);

            if (!mc.player.onGround()) {
                delta *= 0.2;
            }

            if (progress <= 1.0 * speed()) {
                progress += delta;
            } else {
                done = true;
                progress = 1.0;
            }
        }
    }
}