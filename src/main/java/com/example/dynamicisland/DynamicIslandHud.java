package com.example.dynamicisland;

import net.minecraft.client.Minecraft;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.Util;

public class DynamicIslandHud {

    private static float progress = 0f;
    private static float target = 0f;
    private static boolean initialized = false;
    private static long lastMillis;

    public static void setTarget(float t) { target = t; }
    public static float getProgress() { return progress; }

    public static void render(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.options.hideGui) return;

        long now = Util.getMillis();
        if (!initialized) { lastMillis = now; initialized = true; }
        float dt = (now - lastMillis) / 1000f;
        lastMillis = now;

        // 弹性动画
        float speed = 7f;
        progress += (target - progress) * Math.min(1f, dt * speed);
        if (Math.abs(target - progress) < 0.005f) progress = target;

        float eased = easeOutBack(progress);

        int screenWidth = client.getWindow().getGuiScaledWidth();

        // ── 尺寸 ──
        // 收起：窄胶囊；展开：宽矩形
        int baseWidth     = 90;
        int expandedWidth = 340;
        int width = (int) (baseWidth + (expandedWidth - baseWidth) * eased);

        int baseHeight     = 30;
        int expandedHeight = 130;
        int height = (int) (baseHeight + (expandedHeight - baseHeight) * eased);

        // iOS 大圆角：收起时半圆，展开时也保持大圆角
        int radius = height / 2;
        if (eased > 0.3f) {
            // 展开时圆角略小于半高，看起来更像 iOS 展开态
            radius = (int) (height / 2 * (1f - 0.25f * eased));
        }

        int x = (screenWidth - width) / 2;

        int baseY = 6;
        int expandedY = 30;
        int y = (int) (baseY + (expandedY - baseY) * eased);

        // ── 主体：纯黑胶囊 ──
        int bodyColor = 0xFF000000;   // 纯黑不透明
        drawRoundedRect(graphics, x, y, width, height, radius, bodyColor);

        // ── 顶部高光：iOS 的金属反光感 ──
        // 在药丸顶部画一层极淡的半透明白
        int highlightAlpha = (int) (18 + 12 * eased);
        int highlightColor = (highlightAlpha << 24) | 0xFFFFFF;
        drawTopHighlight(graphics, x, y, width, height, radius, highlightColor);

        // ── 内容 ──
        if (eased < 0.5f) {
            // 收起状态：显示玩家数量
            int playerCount = client.getConnection() != null
                    ? client.getConnection().getListedOnlinePlayers().size()
                    : 0;
            String text = String.valueOf(playerCount);
            Font font = client.font;
            int textWidth = font.width(text);
            int textAlpha = (int) (255 * (1f - eased * 2f));
            if (textAlpha > 0) {
                int color = (textAlpha << 24) | 0xFFFFFF;
                graphics.text(font, text,
                        x + (width - textWidth) / 2,
                        y + (height - 8) / 2,
                        color, true);
            }
        }
        // 展开时：玩家列表由原版 Tab 绘制在这个黑色背景上
    }

    /** 顶部高光：一条跟随圆角的浅色弧线 */
    private static void drawTopHighlight(GuiGraphicsExtractor g,
                                         int x, int y, int w, int h,
                                         int r, int color) {
        int highlightThickness = Math.max(1, h / 20);
        // 顶部中间一条短横线
        int inset = r / 2;
        g.fill(x + inset, y, x + w - inset, y + highlightThickness, color);
    }

    /** easeOutBack：带轻微回弹，模拟 iOS 弹性动画 */
    private static float easeOutBack(float t) {
        float c1 = 1.70158f;
        float c3 = c1 + 1f;
        float p = t - 1f;
        return 1f + c3 * p * p * p + c1 * p * p;
    }

    private static void drawRoundedRect(GuiGraphicsExtractor g,
                                        int x, int y, int w, int h,
                                        int r, int color) {
        if (r <= 0) { g.fill(x, y, x + w, y + h, color); return; }
        if (r > h / 2) r = h / 2;
        if (r > w / 2) r = w / 2;

        // 中间大矩形
        g.fill(x + r, y, x + w - r, y + h, color);
        // 左右两侧矩形
        g.fill(x, y + r, x + r, y + h - r, color);
        g.fill(x + w - r, y + r, x + w, y + h - r, color);

        // 四个圆角
        for (int i = 0; i < r; i++) {
            int dx = r - i;
            int dy = (int) Math.sqrt(r * r - dx * dx);
            // 左上
            g.fill(x + i, y + r - dy, x + i + 1, y + r, color);
            // 右上
            g.fill(x + w - i - 1, y + r - dy, x + w - i, y + r, color);
            // 左下
            g.fill(x + i, y + h - r, x + i + 1, y + h - r + dy, color);
            // 右下
            g.fill(x + w - i - 1, y + h - r, x + w - i, y + h - r + dy, color);
        }
    }
}
