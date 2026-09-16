package com.naitool.render;

import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.lwjgl.BufferUtils;

import java.nio.ByteBuffer;

/** 写 projection + modelView 两个 mat4 到 std140 uniform 缓冲（26.1.x 无 DynamicUniformStorage）。 */
public final class MeshUniforms {
    public static final int SIZE = new Std140SizeCalculator()
        .putMat4f()
        .putMat4f()
        .get();

    private MeshUniforms() {}

    /** 返回一个 position=0、limit=SIZE 的可直接交给 createBuffer 的堆外缓冲。 */
    public static ByteBuffer fill(Matrix4fc proj, Matrix4f modelView) {
        ByteBuffer buffer = BufferUtils.createByteBuffer(SIZE);
        Std140Builder.intoBuffer(buffer)
            .putMat4f(proj)
            .putMat4f(modelView);
        buffer.flip();
        return buffer;
    }
}