package com.naitool.mixin;

import java.util.concurrent.atomic.AtomicInteger;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.WritableLevelData;

@Mixin(ClientLevel.class)
public abstract class ClientLevelMixin extends Level {
    @Unique
    private static final AtomicInteger NAITOOL_NEXT_ENTITY_ID = new AtomicInteger(-1);

    protected ClientLevelMixin(WritableLevelData levelData, ResourceKey<Level> dimension,
                               RegistryAccess registryAccess, Holder<DimensionType> dimensionTypeRegistration,
                               boolean isClientSide, boolean isDebug, long biomeZoomSeed, int maxChainedNeighborUpdates) {
        super(levelData, dimension, registryAccess, dimensionTypeRegistration,
                isClientSide, isDebug, biomeZoomSeed, maxChainedNeighborUpdates);
    }

    @Shadow
    public abstract Entity getEntity(int id);

    @Override
    public int getNextEntityId() {
        int id;
        do {
            id = NAITOOL_NEXT_ENTITY_ID.getAndDecrement();
        } while (getEntity(id) != null);
        return id;
    }
}
