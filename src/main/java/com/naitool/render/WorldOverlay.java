package com.naitool.render;

import com.naitool.config.Configs;
import com.naitool.feature.GhostMine;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

/** 世界渲染阶段的 GhostMine 方框绘制驱动。 */
public final class WorldOverlay {
    private static Renderer3D renderer;

    private WorldOverlay() {}

    public static void render(Minecraft mc, Camera camera) {
        if (!GhostMine.isEnabled() || !Configs.Generic.GHOST_MINE_RENDER.getBooleanValue()) {
            return;
        }
        if (mc.player == null || mc.level == null) {
            return;
        }
        if (renderer == null) {
            renderer = new Renderer3D(NaiRenderPipelines.WORLD_COLORED_LINES, NaiRenderPipelines.WORLD_COLORED);
        }

        Vec3 pos = camera.position();

        // V = viewRotation (相机旋转的逆)
        Matrix4f modelView = camera.getViewRotationMatrix(new Matrix4f());

        // P = (P * V) * V^-1
        Matrix4f projection = new Matrix4f(camera.getViewRotationProjectionMatrix(new Matrix4f()))
                .mul(new Matrix4f(modelView).invert());

        renderer.begin(pos);
        GhostMine.draw(renderer, mc);
        renderer.render(mc.getMainRenderTarget(), projection, modelView);
    }
}
