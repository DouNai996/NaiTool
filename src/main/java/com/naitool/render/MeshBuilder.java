package com.naitool.render;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;
import org.lwjgl.BufferUtils;

import java.nio.ByteBuffer;
import java.util.Objects;

import static org.lwjgl.system.MemoryUtil.memAddress0;
import static org.lwjgl.system.MemoryUtil.memCopy;
import static org.lwjgl.system.MemoryUtil.memPutByte;
import static org.lwjgl.system.MemoryUtil.memPutFloat;
import static org.lwjgl.system.MemoryUtil.memPutInt;

/** 相机相对顶点/索引构建器（26.1.x Blaze3D API）。 */
public final class MeshBuilder {
    public double alpha = 1;

    private final VertexFormat format;
    private final int primitiveVerticesSize;

    private ByteBuffer vertices;
    private long verticesPointerStart, verticesPointer;

    private ByteBuffer indices;
    private long indicesPointer;
    private final VertexFormat.IndexType indexType = VertexFormat.IndexType.INT;

    private int vertexI, indicesCount;
    private boolean building;

    private double camX, camY, camZ;

    public MeshBuilder(RenderPipeline pipeline) {
        this(Objects.requireNonNull(pipeline.getVertexFormat(),
            () -> "RenderPipeline " + pipeline.getLocation() + " has no vertex format"));
    }

    public MeshBuilder(VertexFormat format) {
        this.format = format;
        this.primitiveVerticesSize = format.getVertexSize();
    }

    public void begin(double camX, double camY, double camZ) {
        this.camX = camX;
        this.camY = camY;
        this.camZ = camZ;
        this.vertexI = 0;
        this.indicesCount = 0;
        if (vertices != null) {
            this.verticesPointer = this.verticesPointerStart;
        }
        this.building = true;
    }

    public MeshBuilder vec3(double x, double y, double z) {
        long p = verticesPointer;
        memPutFloat(p, (float) (x - camX));
        memPutFloat(p + 4, (float) (y - camY));
        memPutFloat(p + 8, (float) (z - camZ));
        verticesPointer += 12;
        return this;
    }

    public MeshBuilder color(Color c) {
        long p = verticesPointer;
        memPutByte(p, (byte) c.r);
        memPutByte(p + 1, (byte) c.g);
        memPutByte(p + 2, (byte) c.b);
        memPutByte(p + 3, (byte) (c.a * alpha));
        verticesPointer += 4;
        return this;
    }

    public int next() {
        return vertexI++;
    }

    public void line(int i1, int i2) {
        long p = indicesPointer + (long) indicesCount * indexType.bytes;
        memPutInt(p, i1);
        memPutInt(p + indexType.bytes, i2);
        indicesCount += 2;
    }

    public void quad(int i1, int i2, int i3, int i4) {
        long p = indicesPointer + (long) indicesCount * indexType.bytes;
        long b = indexType.bytes;
        memPutInt(p, i1);
        memPutInt(p + b, i2);
        memPutInt(p + b * 2, i3);
        memPutInt(p + b * 3, i3);
        memPutInt(p + b * 4, i4);
        memPutInt(p + b * 5, i1);
        indicesCount += 6;
    }

    public void ensureCapacity(int vertexCount, int indexCount) {
        if (vertices == null || indices == null) {
            allocateBuffers(256 * 4, 512);
            return;
        }

        if ((vertexI + vertexCount) * primitiveVerticesSize >= vertices.capacity()) {
            int offset = getVerticesOffset();
            int newSize = Math.max(vertices.capacity() * 2, vertices.capacity() + vertexCount * primitiveVerticesSize);
            ByteBuffer newVertices = BufferUtils.createByteBuffer(newSize);
            memCopy(memAddress0(vertices), memAddress0(newVertices), offset);
            vertices = newVertices;
            verticesPointerStart = memAddress0(vertices);
            verticesPointer = verticesPointerStart + offset;
        }

        if ((indicesCount + indexCount) * indexType.bytes >= indices.capacity()) {
            int newSize = Math.max(indices.capacity() * 2, indices.capacity() + indexCount * indexType.bytes);
            ByteBuffer newIndices = BufferUtils.createByteBuffer(newSize);
            memCopy(memAddress0(indices), memAddress0(newIndices), (long) indicesCount * indexType.bytes);
            indices = newIndices;
            indicesPointer = memAddress0(indices);
        }
    }

    private void allocateBuffers(int vertexCount, int indexCount) {
        vertices = BufferUtils.createByteBuffer(primitiveVerticesSize * vertexCount);
        verticesPointer = verticesPointerStart = memAddress0(vertices);
        indices = BufferUtils.createByteBuffer(indexCount * indexType.bytes);
        indicesPointer = memAddress0(indices);
    }

    public void end() {
        building = false;
    }

    public boolean isBuilding() {
        return building;
    }

    public GpuBuffer uploadVertexBuffer() {
        vertices.limit(getVerticesOffset());
        return RenderSystem.getDevice().createBuffer(() -> "NaiTool mesh verts", GpuBuffer.USAGE_VERTEX, vertices);
    }

    public GpuBuffer uploadIndexBuffer() {
        indices.limit(indicesCount * indexType.bytes);
        return RenderSystem.getDevice().createBuffer(() -> "NaiTool mesh idx", GpuBuffer.USAGE_INDEX, indices);
    }

    public int getIndicesCount() {
        return indicesCount;
    }

    public VertexFormat.IndexType getIndexType() {
        return indexType;
    }

    private int getVerticesOffset() {
        return (int) (verticesPointer - verticesPointerStart);
    }
}