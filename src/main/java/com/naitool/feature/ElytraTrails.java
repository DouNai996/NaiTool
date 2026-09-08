package com.naitool.feature;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.TrailParticleOption;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

import fi.dy.masa.malilib.config.IConfigOptionListEntry;
import com.naitool.config.Configs;

public final class ElytraTrails {
    private ElytraTrails() {}

    public enum TrailMode implements IConfigOptionListEntry {
        NORMAL("normal", "naitool.config.generic.elytraTrailsMode.normal"),
        RAINBOW("rainbow", "naitool.config.generic.elytraTrailsMode.rainbow"),
        SPIRAL("spiral", "naitool.config.generic.elytraTrailsMode.spiral"),
        MULTI("multi", "naitool.config.generic.elytraTrailsMode.multi"),
        WINGS("wings", "naitool.config.generic.elytraTrailsMode.wings");

        private final String stringValue;
        private final String displayNameKey;

        TrailMode(String stringValue, String displayNameKey) {
            this.stringValue = stringValue;
            this.displayNameKey = displayNameKey;
        }

        @Override
        public String getStringValue() { return stringValue; }

        @Override
        public String getDisplayName() {
            return fi.dy.masa.malilib.util.StringUtils.translate(displayNameKey);
        }

        @Override
        public IConfigOptionListEntry cycle(boolean forward) {
            int id = ordinal();
            TrailMode[] values = values();
            return values[(forward ? id + 1 : id - 1 + values.length) % values.length];
        }

        @Override
        public IConfigOptionListEntry fromString(String value) {
            for (TrailMode mode : values()) {
                if (mode.stringValue.equalsIgnoreCase(value)) return mode;
            }
            return NORMAL;
        }
    }

    private static double p0x, p0y, p0z;
    private static double p1x, p1y, p1z;
    private static int trailPosCount;
    private static int tickCount;

    public static boolean isPlayerGliding() {
        Minecraft mc = Minecraft.getInstance();
        return mc.player != null && mc.player.isFallFlying();
    }

    public static boolean shouldHideFireworks() {
        return Configs.Generic.ELYTRA_TRAILS_HIDE_FIREWORKS.getBooleanValue();
    }

    public static void tick() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;
        if (!Configs.Generic.ELYTRA_TRAILS_ENABLED.getBooleanValue()) {
            trailPosCount = 0;
            return;
        }
        if (!mc.player.isFallFlying()) {
            trailPosCount = 0;
            return;
        }
        if (!mc.player.getItemBySlot(EquipmentSlot.CHEST).is(Items.ELYTRA)) {
            trailPosCount = 0;
            return;
        }

        tickCount++;
        TrailMode mode = (TrailMode) Configs.Generic.ELYTRA_TRAILS_MODE.getOptionListValue();
        int baseColor = Configs.Generic.ELYTRA_TRAILS_COLOR.getIntegerValue();
        int duration = Configs.Generic.ELYTRA_TRAILS_DURATION.getIntegerValue();
        double spread = Configs.Generic.ELYTRA_TRAILS_SPREAD.getDoubleValue();
        int density = Configs.Generic.ELYTRA_TRAILS_DENSITY.getIntegerValue();
        double yOffset = Configs.Generic.ELYTRA_TRAILS_Y_OFFSET.getDoubleValue();

        Vec3 velocity = mc.player.getDeltaMovement();
        Vec3 perp = velocity.normalize().cross(new Vec3(0, 1, 0));
        if (perp.length() < 0.01) perp = new Vec3(1, 0, 0);
        perp = perp.normalize();
        Vec3 up = perp.cross(velocity.normalize()).normalize();

        Vec3 look = mc.player.getLookAngle().scale(-0.8);
        double cx = mc.player.getX() + look.x;
        double cy = mc.player.getY() - yOffset;
        double cz = mc.player.getZ() + look.z;

        Vec3 p2 = new Vec3(cx, cy, cz);
        Vec3 p1 = trailPosCount >= 1 ? new Vec3(p1x, p1y, p1z) : p2;
        Vec3 p0 = trailPosCount >= 2 ? new Vec3(p0x, p0y, p0z) : p1;

        ClientLevel clientLevel = mc.level;

        switch (mode) {
            case RAINBOW -> spawnRainbow(clientLevel, p0, p1, p2, perp, up, density, spread, duration);
            case SPIRAL -> spawnSpiral(clientLevel, p2, perp, up, density, spread, duration);
            case MULTI -> spawnMulti(clientLevel, p0, p1, p2, perp, baseColor, density, spread, duration);
            case WINGS -> spawnWings(clientLevel, p0, p1, p2, perp, up, baseColor, density, spread, duration);
            default -> spawnNormal(clientLevel, p0, p1, p2, baseColor, density, duration);
        }

        p0x = p1x; p0y = p1y; p0z = p1z;
        p1x = cx;  p1y = cy;  p1z = cz;
        trailPosCount = Math.min(trailPosCount + 1, 2);
    }

    private static void spawnNormal(ClientLevel level, Vec3 p0, Vec3 p1, Vec3 p2,
                                     int color, int count, int duration) {
        int brightColor = brighten(color);
        for (int i = 0; i < count; i++) {
            Vec3 pos = bezier(p0, p1, p2, (float) i / count);
            level.addParticle(new TrailParticleOption(pos, brightColor, duration),
                    pos.x, pos.y, pos.z, 0, 0, 0);
        }
    }

    private static void spawnRainbow(ClientLevel level, Vec3 p0, Vec3 p1, Vec3 p2,
                                      Vec3 perp, Vec3 up, int count, double spread, int duration) {
        var rng = Minecraft.getInstance().player.getRandom();
        for (int i = 0; i < count; i++) {
            float t = (float) i / count;
            Vec3 pos = bezier(p0, p1, p2, t);
            float hue = ((tickCount * 2 + i * 3) % 360) / 360f;
            int color = java.awt.Color.HSBtoRGB(hue, 0.9f, 1.0f);

            if (spread > 0) {
                float angle = rng.nextFloat() * (float) Math.PI * 2;
                double r = rng.nextFloat() * spread * 0.3;
                pos = pos.add(perp.scale(Math.cos(angle) * r)).add(up.scale(Math.sin(angle) * r));
            }
            Vec3 target = new Vec3(
                    pos.x + perp.x * Math.cos(t * Math.PI * 4 + tickCount * 0.1) * spread * 0.2,
                    pos.y + perp.y * Math.cos(t * Math.PI * 4 + tickCount * 0.1) * spread * 0.2 + Math.sin(t * Math.PI * 4 + tickCount * 0.1) * spread * 0.2,
                    pos.z + perp.z * Math.cos(t * Math.PI * 4 + tickCount * 0.1) * spread * 0.2
            );
            level.addParticle(new TrailParticleOption(target, color, duration),
                    pos.x, pos.y, pos.z, 0, 0, 0);
        }
    }

    private static void spawnSpiral(ClientLevel level, Vec3 center, Vec3 perp, Vec3 up,
                                     int count, double radius, int duration) {
        int strands = 3;
        for (int s = 0; s < strands; s++) {
            double phaseOffset = s * (2.0 * Math.PI / strands);
            for (int i = 0; i < count; i++) {
                float t = (float) i / count;
                float angle = t * (float) Math.PI * 6 + tickCount * 0.15f + (float) phaseOffset;
                double r = radius * (0.3 + 0.7 * t);
                double ox = Math.cos(angle) * r;
                double oy = Math.sin(angle) * r;
                Vec3 pos = center.add(perp.scale(ox)).add(up.scale(oy));

                float hue = ((tickCount * 3 + s * 120 + i * 2) % 360) / 360f;
                int color = java.awt.Color.HSBtoRGB(hue, 0.85f, 1.0f);

                level.addParticle(new TrailParticleOption(pos, color, duration),
                        pos.x, pos.y, pos.z, 0, 0, 0);
            }
        }
    }

    private static void spawnMulti(ClientLevel level, Vec3 p0, Vec3 p1, Vec3 p2,
                                    Vec3 perp, int color, int count, double spread, int duration) {
        var rng = Minecraft.getInstance().player.getRandom();
        int lines = 5;
        double spacing = Math.max(0.3, spread * 0.4);
        for (int trailIdx = -(lines / 2); trailIdx <= lines / 2; trailIdx++) {
            Vec3 offset = perp.scale(trailIdx * spacing);
            float hueShift = trailIdx * 0.15f;
            for (int i = 0; i < count; i++) {
                float t = (float) i / count;
                Vec3 pos = bezier(p0, p1, p2, t).add(offset);
                float hue = ((tickCount * 2 + i * 3 + hueShift * 360) % 360) / 360f;
                int c = java.awt.Color.HSBtoRGB(hue, 0.8f, 1.0f);
                double rx = (rng.nextFloat() - 0.5) * spread * 0.3;
                double ry = (rng.nextFloat() - 0.5) * spread * 0.3;
                double rz = (rng.nextFloat() - 0.5) * spread * 0.3;
                Vec3 target = new Vec3(pos.x + perp.x * rx + rx, pos.y + perp.y * rx + ry, pos.z + perp.z * rx + rz);
                level.addParticle(new TrailParticleOption(target, c, duration),
                        pos.x, pos.y, pos.z, 0, 0, 0);
            }
        }
    }

    private static void spawnWings(ClientLevel level, Vec3 p0, Vec3 p1, Vec3 p2,
                                    Vec3 perp, Vec3 up, int color, int count, double spread, int duration) {
        for (int side = -1; side <= 1; side += 2) {
            Vec3 wingBase = perp.scale(side * 0.9).add(up.scale(0.3)).add(perp.scale(side * 0.2));
            for (int i = 0; i < count; i++) {
                float t = (float) i / count;
                Vec3 pos = bezier(p0, p1, p2, t).add(wingBase.scale(0.5 + t * 0.5));

                float flapOffset = (float)(Math.sin(tickCount * 0.2) * 0.15 * side);
                pos = pos.add(up.scale(flapOffset));

                float hue = ((tickCount * 2 + i * 4 + (side > 0 ? 180 : 0)) % 360) / 360f;
                int c = java.awt.Color.HSBtoRGB(hue, 0.85f, 1.0f);

                Vec3 trailDir = perp.scale(side * (0.3 + t * 0.5 * spread));
                Vec3 target = pos.add(trailDir).add(up.scale(t * spread * 0.2));

                level.addParticle(new TrailParticleOption(target, c, duration),
                        pos.x, pos.y, pos.z, 0, 0, 0);
            }
        }
        for (int i = 0; i < count / 2; i++) {
            float t = (float) i / (count / 2);
            Vec3 pos = bezier(p0, p1, p2, t);
            int c = brighten(color);
            level.addParticle(new TrailParticleOption(pos, c, duration),
                    pos.x, pos.y, pos.z, 0, 0, 0);
        }
    }

    private static Vec3 bezier(Vec3 p0, Vec3 p1, Vec3 p2, float t) {
        float u = 1 - t;
        return new Vec3(
                u * u * p0.x + 2 * u * t * p1.x + t * t * p2.x,
                u * u * p0.y + 2 * u * t * p1.y + t * t * p2.y,
                u * u * p0.z + 2 * u * t * p1.z + t * t * p2.z
        );
    }

    private static int brighten(int color) {
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;
        r = Math.min(255, r + (255 - r) * 2 / 5);
        g = Math.min(255, g + (255 - g) * 2 / 5);
        b = Math.min(255, b + (255 - b) * 2 / 5);
        return (r << 16) | (g << 8) | b;
    }
}