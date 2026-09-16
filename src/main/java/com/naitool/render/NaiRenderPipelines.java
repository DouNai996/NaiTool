package com.naitool.render;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.resources.Identifier;
import com.naitool.Reference;

/** 两条世界渲染管线（26.1.x：withVertexFormat + 直接 withUniform，无 BindGroupLayout）。 */
public final class NaiRenderPipelines {
    private NaiRenderPipelines() {}

    public static final RenderPipeline WORLD_COLORED = RenderPipeline.builder()
            .withLocation(id("pipeline/world_colored"))
            .withVertexShader(id("pos_color"))
            .withFragmentShader(id("pos_color"))
            .withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.TRIANGLES)
            .withUniform("MeshData", UniformType.UNIFORM_BUFFER)
            .withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false))
            .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
            .withCull(false)
            .build();

    public static final RenderPipeline WORLD_COLORED_LINES = RenderPipeline.builder()
            .withLocation(id("pipeline/world_colored_lines"))
            .withVertexShader(id("pos_color"))
            .withFragmentShader(id("pos_color"))
            .withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.DEBUG_LINES)
            .withUniform("MeshData", UniformType.UNIFORM_BUFFER)
            .withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false))
            .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
            .withCull(false)
            .build();

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(Reference.MOD_ID, path);
    }
}