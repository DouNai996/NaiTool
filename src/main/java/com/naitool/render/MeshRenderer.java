package com.naitool.render;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTextureView;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;

import java.nio.ByteBuffer;
import java.util.OptionalInt;

/** 单次 Mesh 绘制（26.1.x：createBuffer 上传，setVertexBuffer 传 GpuBuffer，drawIndexed 4 参）。 */
public final class MeshRenderer {
    private MeshRenderer() {}

    public static void draw(CommandEncoder encoder,
                            GpuTextureView colorView,
                            RenderPipeline pipeline,
                            MeshBuilder mesh,
                            Matrix4fc projection,
                            Matrix4f modelView) {
        int indexCount = mesh.getIndicesCount();
        if (indexCount <= 0) {
            return;
        }

        ByteBuffer uniformData = MeshUniforms.fill(projection, modelView);

        try (GpuBuffer uniformBuffer = RenderSystem.getDevice().createBuffer(
                () -> "NaiTool mesh ubo", GpuBuffer.USAGE_UNIFORM, uniformData); GpuBuffer vertexBuffer = mesh.uploadVertexBuffer(); GpuBuffer indexBuffer = mesh.uploadIndexBuffer(); RenderPass pass = encoder.createRenderPass(() -> "NaiTool GhostMine", colorView, OptionalInt.empty())) {
            pass.setPipeline(pipeline);
            pass.setUniform("MeshData", uniformBuffer.slice());
            pass.setVertexBuffer(0, vertexBuffer);
            pass.setIndexBuffer(indexBuffer, mesh.getIndexType());
            // 26.1.x 签名：drawIndexed(firstVertex, firstIndex, indexCount, instanceCount)
            pass.drawIndexed(0, 0, indexCount, 1);
        }
    }
}