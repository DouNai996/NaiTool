package com.naitool.render;

/** 极简 RGBA 颜色容器（0-255 各通道），供网格顶点写入。 */
public final class Color {
    public int r, g, b, a;

    public Color(int r, int g, int b, int a) {
        this.r = clamp(r);
        this.g = clamp(g);
        this.b = clamp(b);
        this.a = clamp(a);
    }

    /** 从 0xRRGGBB + alpha 构造 */
    public static Color of(int rgb, int alpha) {
        return new Color((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF, alpha & 0xFF);
    }

    /** 替换 alpha 通道 */
    public Color withAlpha(int alpha) {
        return new Color(r, g, b, clamp(alpha));
    }

    /** 按系数缩放 RGB（>1 向白色提亮），alpha 不变 */
    public Color mulRGB(double factor) {
        return new Color((int) (r * factor), (int) (g * factor), (int) (b * factor), a);
    }

    /** 整体（含 alpha）乘系数 */
    public Color mulAll(double factor) {
        return new Color((int) (r * factor), (int) (g * factor), (int) (b * factor), (int) (a * factor));
    }

    /** 线性插值 */
    public static Color lerp(Color from, Color to, double t) {
        if (t <= 0) {
            return from;
        }
        if (t >= 1) {
            return to;
        }
        return new Color(
                (int) (from.r + (to.r - from.r) * t),
                (int) (from.g + (to.g - from.g) * t),
                (int) (from.b + (to.b - from.b) * t),
                (int) (from.a + (to.a - from.a) * t));
    }

    private static int clamp(int v) {
        return v < 0 ? 0 : Math.min(v, 255);
    }
}
