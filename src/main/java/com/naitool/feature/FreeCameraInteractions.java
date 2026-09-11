package com.naitool.feature;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import com.naitool.config.Configs;

public final class FreeCameraInteractions {
    private static final String TWEAKEROO_CAMERA_CLASS = "fi.dy.masa.tweakeroo.util.CameraEntity";

    private FreeCameraInteractions() {
    }

    public static boolean shouldOverrideCrosshair(Minecraft client) {
        return Configs.Generic.FREE_CAMERA_ENABLED.getBooleanValue()
                && isTweakerooFreeCameraActive(client);
    }

    public static Entity getEasyPlaceTraceEntity(Minecraft client, Entity originalEntity) {
        if (!Configs.Generic.FREE_CAMERA_ENABLED.getBooleanValue()
                || !Configs.Generic.FREE_CAMERA_EASY_PLACE.getBooleanValue()
                || !isTweakerooFreeCameraActive(client)) {
            return originalEntity;
        }

        Entity camera = client.getCameraEntity();
        return camera != null ? camera : originalEntity;
    }

    public static HitResult filterCrosshairTarget(Minecraft client, Entity camera, HitResult target) {
        if (target instanceof BlockHitResult && !Configs.Generic.FREE_CAMERA_BLOCK_INTERACTIONS.getBooleanValue()) {
            return createMiss(camera, target.getLocation());
        }

        if (target instanceof EntityHitResult entityHitResult
                && (!Configs.Generic.FREE_CAMERA_ENTITY_INTERACTIONS.getBooleanValue()
                || entityHitResult.getEntity() == client.player)) {
            return createMiss(camera, target.getLocation());
        }

        return target;
    }

    public static boolean isBlockOutsideServerInteractionRange(Minecraft client, BlockPos position) {
        return shouldOverrideCrosshair(client)
                && !client.player.isWithinBlockInteractionRange(position, 1.0);
    }

    public static boolean isEntityOutsideServerInteractionRange(Minecraft client, Entity entity) {
        return shouldOverrideCrosshair(client)
                && !client.player.isWithinEntityInteractionRange(entity.getBoundingBox(), 1.0);
    }

    private static boolean isTweakerooFreeCameraActive(Minecraft client) {
        if (client == null || client.player == null) {
            return false;
        }

        Entity camera = client.getCameraEntity();
        return camera != null
                && camera != client.player
                && TWEAKEROO_CAMERA_CLASS.equals(camera.getClass().getName());
    }

    private static BlockHitResult createMiss(Entity camera, Vec3 position) {
        Vec3 rotation = camera.getViewVector(1.0F);
        return BlockHitResult.miss(
                position,
                Direction.getApproximateNearest(rotation.x, rotation.y, rotation.z),
                BlockPos.containing(position)
        );
    }
}