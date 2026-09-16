package com.naitool.render;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTextureView;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;

/** 世界坐标方框/线段绘制（移植自 Meteor Renderer3D，去掉方向剔除，仅画完整框）。 */
public final class Renderer3D {
    public final MeshBuilder lines;
    public final MeshBuilder triangles;
    private final RenderPipeline linesPipeline;
    private final RenderPipeline trianglesPipeline;

    public Renderer3D(RenderPipeline lines, RenderPipeline triangles) {
        this.linesPipeline = lines;
        this.trianglesPipeline = triangles;
        this.lines = new MeshBuilder(lines);
        this.triangles = new MeshBuilder(triangles);
    }

    public void begin(Vec3 camera) {
        lines.begin(camera.x, camera.y, camera.z);
        triangles.begin(camera.x, camera.y, camera.z);
    }

    public void render(RenderTarget target, Matrix4fc projection, Matrix4f modelView) {
        CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
        GpuTextureView color = target.getColorTextureView();

        if (lines.isBuilding()) {
            lines.end();
        }
        MeshRenderer.draw(encoder, color, linesPipeline, lines, projection, modelView);

        if (triangles.isBuilding()) {
            triangles.end();
        }
        MeshRenderer.draw(encoder, color, trianglesPipeline, triangles, projection, modelView);
    }

    // ===== Lines =====

    public void boxLines(double x1, double y1, double z1, double x2, double y2, double z2, Color color) {
        lines.ensureCapacity(8, 24);

        int blb = lines.vec3(x1, y1, z1).color(color).next();
        int blf = lines.vec3(x1, y1, z2).color(color).next();
        int brb = lines.vec3(x2, y1, z1).color(color).next();
        int brf = lines.vec3(x2, y1, z2).color(color).next();
        int tlb = lines.vec3(x1, y2, z1).color(color).next();
        int tlf = lines.vec3(x1, y2, z2).color(color).next();
        int trb = lines.vec3(x2, y2, z1).color(color).next();
        int trf = lines.vec3(x2, y2, z2).color(color).next();

        // 竖棱
        lines.line(blb, tlb);
        lines.line(blf, tlf);
        lines.line(brb, trb);
        lines.line(brf, trf);
        // 底环
        lines.line(blb, blf);
        lines.line(brb, brf);
        lines.line(blb, brb);
        lines.line(blf, brf);
        // 顶环
        lines.line(tlb, tlf);
        lines.line(trb, trf);
        lines.line(tlb, trb);
        lines.line(tlf, trf);
    }

    // ===== Sides =====

    public void boxSides(double x1, double y1, double z1, double x2, double y2, double z2, Color color) {
        triangles.ensureCapacity(8, 36);

        int blb = triangles.vec3(x1, y1, z1).color(color).next();
        int blf = triangles.vec3(x1, y1, z2).color(color).next();
        int brb = triangles.vec3(x2, y1, z1).color(color).next();
        int brf = triangles.vec3(x2, y1, z2).color(color).next();
        int tlb = triangles.vec3(x1, y2, z1).color(color).next();
        int tlf = triangles.vec3(x1, y2, z2).color(color).next();
        int trb = triangles.vec3(x2, y2, z1).color(color).next();
        int trf = triangles.vec3(x2, y2, z2).color(color).next();

        triangles.quad(blb, blf, tlf, tlb);
        triangles.quad(brb, trb, trf, brf);
        triangles.quad(blb, tlb, trb, brb);
        triangles.quad(blf, brf, trf, tlf);
        triangles.quad(blb, brb, brf, blf); // bottom
        triangles.quad(tlb, tlf, trf, trb); // top
    }

    public void box(double x1, double y1, double z1, double x2, double y2, double z2,
                    Color sideColor, Color lineColor, ShapeMode mode) {
        if (mode.lines()) {
            boxLines(x1, y1, z1, x2, y2, z2, lineColor);
        }
        if (mode.sides()) {
            boxSides(x1, y1, z1, x2, y2, z2, sideColor);
        }
    }

    public void box(BlockPos pos, Color sideColor, Color lineColor, ShapeMode mode) {
        box(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1,
            sideColor, lineColor, mode);
    }

    // ===== 现代化动画原语 =====

    /** 盒子 8 角点顺序：0-3 底环，4-7 顶环；12 条棱的端点索引。 */
    private static final int[][] EDGES = {
            {0, 4}, {1, 5}, {2, 6}, {3, 7}, // 竖棱
            {0, 1}, {2, 3}, {0, 2}, {1, 3}, // 底环
            {4, 5}, {6, 7}, {4, 6}, {5, 7}  // 顶环
    };

    private static double[][] corners(double x1, double y1, double z1, double x2, double y2, double z2) {
        return new double[][] {
                {x1, y1, z1}, {x1, y1, z2}, {x2, y1, z1}, {x2, y1, z2},
                {x1, y2, z1}, {x1, y2, z2}, {x2, y2, z1}, {x2, y2, z2}
        };
    }

    private interface EdgeColor {
        Color at(double u);
    }

    /** 沿 12 条棱细分绘制，每段颜色由 callback 按棱上参数 u(0~1) 决定。 */
    private void animatedEdges(double x1, double y1, double z1, double x2, double y2, double z2,
                               int segments, EdgeColor colorFn) {
        lines.ensureCapacity(12 * (segments + 1), 24 * segments);
        double[][] c = corners(x1, y1, z1, x2, y2, z2);

        for (int[] edge : EDGES) {
            double[] a = c[edge[0]];
            double[] b = c[edge[1]];
            int prev = -1;
            for (int k = 0; k <= segments; k++) {
                double u = (double) k / segments;
                int idx = lines.vec3(
                        a[0] + (b[0] - a[0]) * u,
                        a[1] + (b[1] - a[1]) * u,
                        a[2] + (b[2] - a[2]) * u)
                        .color(colorFn.at(u)).next();
                if (prev != -1) {
                    lines.line(prev, idx);
                }
                prev = idx;
            }
        }
    }

    /**
     * 能量流光描边：一道高亮沿 12 条棱循环流动。
     *
     * @param phase 流光临界位置 0~1（随时间推进）
     */
    public void boxLinesFlow(double x1, double y1, double z1, double x2, double y2, double z2,
                             Color base, double phase, int segments) {
        double p = phase % 1.0;
        animatedEdges(x1, y1, z1, x2, y2, z2, segments, u -> {
            double d = Math.abs(u - p);
            d = Math.min(d, 1.0 - d);
            double glow = Math.pow(Math.max(0.0, 1.0 - d * 4.5), 2.0);
            return Color.lerp(base, new Color(255, 255, 255, base.a), glow * 0.9);
        });
    }

    /**
     * 蚂蚁行军虚线描边：明暗虚线沿棱流动。
     *
     * @param shift 虚线段滚动位置（随时间推进，可为负）
     */
    public void boxLinesMarching(double x1, double y1, double z1, double x2, double y2, double z2,
                                 Color on, Color off, double shift, int segments) {
        final int period = 4;
        final int duty = 2;
        final int offset = (int) Math.floor(shift);
        animatedEdges(x1, y1, z1, x2, y2, z2, segments, u -> {
            int s = (int) (u * segments);
            boolean lit = ((s + offset) % period + period) % period < duty;
            return lit ? on : off;
        });
    }

    /** 指定高度的水平扫描环（向外微扩）。 */
    public void ringAtY(double x1, double z1, double x2, double z2, double y, double expand, Color color) {
        lines.ensureCapacity(4, 8);
        int bl = lines.vec3(x1 - expand, y, z1 - expand).color(color).next();
        int fl = lines.vec3(x1 - expand, y, z2 + expand).color(color).next();
        int fr = lines.vec3(x2 + expand, y, z2 + expand).color(color).next();
        int br = lines.vec3(x2 + expand, y, z1 - expand).color(color).next();
        lines.line(bl, fl);
        lines.line(fl, fr);
        lines.line(fr, br);
        lines.line(br, bl);
    }

    /** 指定高度的半透明水平扫描面。 */
    public void planeAtY(double x1, double z1, double x2, double z2, double y, Color color) {
        triangles.ensureCapacity(4, 6);
        int bl = triangles.vec3(x1, y, z1).color(color).next();
        int fl = triangles.vec3(x1, y, z2).color(color).next();
        int fr = triangles.vec3(x2, y, z2).color(color).next();
        int br = triangles.vec3(x2, y, z1).color(color).next();
        triangles.quad(bl, fl, fr, br);
    }

    /**
     * 8 个角的直角括角（战术瞄准框风格），段长沿各轴向内。
     * 每角 4 顶点 3 段，共 32 顶点 48 索引。
     */
    public void cornerBrackets(double x1, double y1, double z1, double x2, double y2, double z2,
                               double len, Color color) {
        lines.ensureCapacity(32, 48);
        for (int cx = 0; cx < 2; cx++) {
            double x = cx == 0 ? x1 : x2;
            double ix = cx == 0 ? len : -len;
            for (int cy = 0; cy < 2; cy++) {
                double y = cy == 0 ? y1 : y2;
                double iy = cy == 0 ? len : -len;
                for (int cz = 0; cz < 2; cz++) {
                    double z = cz == 0 ? z1 : z2;
                    double iz = cz == 0 ? len : -len;
                    int o = lines.vec3(x, y, z).color(color).next();
                    int vx = lines.vec3(x + ix, y, z).color(color).next();
                    int vy = lines.vec3(x, y + iy, z).color(color).next();
                    int vz = lines.vec3(x, y, z + iz).color(color).next();
                    lines.line(o, vx);
                    lines.line(o, vy);
                    lines.line(o, vz);
                }
            }
        }
    }

    /** 绕 (cx, cy, cz) 旋转的水平正方形能量环，half 为环心到顶点距离，angle 为弧度。 */
    public void rotatingRing(double cx, double cy, double cz, double half, double angle, Color color) {
        lines.ensureCapacity(4, 8);
        int[] idx = new int[4];
        for (int k = 0; k < 4; k++) {
            double t = angle + k * Math.PI / 2.0;
            idx[k] = lines.vec3(cx + half * Math.cos(t), cy, cz + half * Math.sin(t)).color(color).next();
        }
        for (int k = 0; k < 4; k++) {
            lines.line(idx[k], idx[(k + 1) % 4]);
        }
    }
}
