package com.example.dynamicisland;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.render.DeltaTracker;
import net.minecraft.util.Util;

public class DynamicIslandHud {

    private static float progress = 0f;   // 0 = 收起，1 = 展开
    private static float target = 0f;
    private static boolean initialized = false;
    private static long lastMillis;

    /** 由 PlayerListHudMixin 调用 */
    public static void setTarget(float t) {
        target = t;
    }

    public static void render(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.options.hudHidden) return;

        // 时间用现实世界毫秒
        long now = Util.getMillis();
        if (!initialized) { lastMillis = now; initialized = true; }
        float dt = (now - lastMillis) / 1000f;
        lastMillis = now;

        // 平滑动画
        float speed = 8f;
        progress += (target - progress) * Math.min(1f, dt * speed);
        if (Math.abs(target - progress) < 0.005f) progress = target;
        if (progress <= 0.01f) return;

        int screenWidth = client.getWindow().getScaledWidth();
        int baseWidth = 44, expandedWidth = 180, height = 24;
        int width = (int) (baseWidth + (expandedWidth - baseWidth) * progress);
        int x = (screenWidth - width) / 2;
        int y = 8;

        int alpha = (int) (255 * progress);
        // ARGB（1.21.6+ 文本颜色必须带 alpha）
        int bgColor   = (alpha << 24) | 0x1A1A1A;
        int textColor = (alpha << 24) | 0xFFFFFF;

        drawRoundedRect(graphics, x, y, width, height, 12, bgColor);

        int playerCount = client.getNetworkHandler() != null
                ? client.getNetworkHandler().getPlayerList().size()
                : 0;
        String text = "玩家列表 " + playerCount;
        TextRenderer font = client.textRenderer;
        int textWidth = font.getWidth(text);
        graphics.text(
                font,
                text,
                x + (width - textWidth) / 2,
                y + (height - 8) / 2,
                textColor,
                true
        );
    }

    private static void drawRoundedRect(GuiGraphicsExtractor g,
                                        int x, int y, int w, int h,
                                        int r, int color) {
        g.fill(x + r, y, x + w - r, y + h, color);
        g.fill(x, y + r, x + r, y + h - r, color);
        g.fill(x + w - r, y + r, x + w, y + h - r, color);
        for (int i = 0; i < r; i++) {
            int dx = r - i;
            int dy = (int) Math.sqrt(r * r - dx * dx);
            g.fill(x + i, y + r - dy, x + i + 1, y + r, color);
            g.fill(x + w - i - 1, y + r - dy, x + w - i, y + r, color);
            g.fill(x + i, y + h - r, x + i + 1, y + h - r + dy, color);
            g.fill(x + w - i - 1, y + h - r, x + w - i, y + h - r + dy, color);
        }
    }
}
