package com.naitool.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import com.naitool.feature.FreeCameraInteractions;
import fi.dy.masa.litematica.util.WorldUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;

@Mixin(value = WorldUtils.class, remap = false)
public final class LitematicaFreeCameraMixin {
    @ModifyArg(
            method = "doEasyPlaceAction",
            at = @At(
                    value = "INVOKE",
                    target = "Lfi/dy/masa/litematica/util/RayTraceUtils;getGenericTrace(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/Entity;DZZZ)Lfi/dy/masa/litematica/util/RayTraceUtils$RayTraceWrapper;"
            ),
            index = 1,
            remap = false
    )
    private static Entity naitool$useFreeCameraForNearestSchematicTrace(Entity originalEntity) {
        return FreeCameraInteractions.getEasyPlaceTraceEntity(Minecraft.getInstance(), originalEntity);
    }

    @ModifyArg(
            method = "doEasyPlaceAction",
            at = @At(
                    value = "INVOKE",
                    target = "Lfi/dy/masa/litematica/util/RayTraceUtils;getFurthestSchematicWorldTraceBeforeVanilla(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/Entity;D)Lfi/dy/masa/litematica/util/RayTraceUtils$RayTraceWrapper;"
            ),
            index = 1,
            remap = false
    )
    private static Entity naitool$useFreeCameraForFurthestSchematicTrace(Entity originalEntity) {
        return FreeCameraInteractions.getEasyPlaceTraceEntity(Minecraft.getInstance(), originalEntity);
    }

    @ModifyArg(
            method = "doEasyPlaceAction",
            at = @At(
                    value = "INVOKE",
                    target = "Lfi/dy/masa/litematica/util/RayTraceUtils;getRayTraceFromEntity(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/Entity;ZD)Lnet/minecraft/world/phys/HitResult;"
            ),
            index = 1,
            remap = false
    )
    private static Entity naitool$useFreeCameraForVanillaTrace(Entity originalEntity) {
        return FreeCameraInteractions.getEasyPlaceTraceEntity(Minecraft.getInstance(), originalEntity);
    }
}