package com.example.dynamicisland;

import net.minecraft.client.Minecraft;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;

public class DynamicIslandHud {

    private static final Identifier NETEASE_ICON =
            Identifier.fromNamespaceAndPath("dynamic-island", "textures/gui/netease.png");
    private static final Identifier SPOTIFY_ICON =
            Identifier.fromNamespaceAndPath("dynamic-island", "textures/gui/spotify.png");

    // 图片原始尺寸，按你实际图片改
    private static final int ICON_TEX_SIZE = 64;

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

        float speed = 7f;
        progress += (target - progress) * Math.min(1f, dt * speed);
        if (Math.abs(target - progress) < 0.005f) progress = target;

        float eased = easeOutBack(progress);

        int screenWidth = client.getWindow().getGuiScaledWidth();

        int baseWidth     = 90;
        int expandedWidth = 340;
        int width = (int) (baseWidth + (expandedWidth - baseWidth) * eased);

        int baseHeight     = 30;
        int expandedHeight = 130;
        int height = (int) (baseHeight + (expandedHeight - baseHeight) * eased);

        int radius = height / 2;
        if (eased > 0.3f) {
            radius = (int) (height / 2 * (1f - 0.25f * eased));
        }

        int x = (screenWidth - width) / 2;

        int baseY = 6;
        int expandedY = 30;
        int y = (int) (baseY + (expandedY - baseY) * eased);

        // ── 纯黑药丸 ──
        int bodyColor = 0xFF000000;
        drawRoundedRect(graphics, x, y, width, height, radius, bodyColor);

        // ── 顶部高光 ──
        int highlightAlpha = (int) (18 + 12 * eased);
        int highlightColor = (highlightAlpha << 24) | 0xFFFFFF;
        drawTopHighlight(graphics, x, y, width, height, radius, highlightColor);

        // ── 内容 ──
        if (eased < 0.5f) {
            renderCompact(graphics, client, x, y, width, height, eased);
        }
        // 展开时玩家列表由原版 Tab 绘制在这个黑背景上
    }

    /** 收起状态：左侧音乐图标 + 右侧玩家数量 */
    private static void renderCompact(GuiGraphicsExtractor graphics,
                                      Minecraft client,
                                      int x, int y, int width, int height,
                                      float eased) {
        int textAlpha = (int) (255 * (1f - eased * 2f));
        if (textAlpha <= 0) return;

        Font font = client.font;

        int playerCount = client.getConnection() != null
                ? client.getConnection().getListedOnlinePlayers().size()
                : 0;
        String countText = String.valueOf(playerCount);
        int countWidth = font.width(countText);
        int countColor = (textAlpha << 24) | 0xFFFFFF;

        MediaProcessDetector.Player player = MediaProcessDetector.getCurrentPlayer();
        boolean hasIcon = player != MediaProcessDetector.Player.NONE;

        int iconSize = height - 10;
        int iconPadding = 7;

        if (hasIcon) {
            int iconX = x + iconPadding;
            int iconY = y + (height - iconSize) / 2;

            Identifier icon = (player == MediaProcessDetector.Player.NETEASE)
                    ? NETEASE_ICON : SPOTIFY_ICON;

            graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                icon,
                iconX, iconY,
                0, 0,
                iconSize, iconSize,
                ICON_TEX_SIZE, ICON_TEX_SIZE
            );

            int countX = x + width - countWidth - iconPadding;
            graphics.text(font, countText, countX, y + (height - 8) / 2, countColor, true);
        } else {
            graphics.text(font, countText,
                    x + (width - countWidth) / 2,
                    y + (height - 8) / 2,
                    countColor, true);
        }
    }

    private static void drawTopHighlight(GuiGraphicsExtractor g,
                                         int x, int y, int w, int h,
                                         int r, int color) {
        int thickness = Math.max(1, h / 20);
        int inset = r / 2;
        g.fill(x + inset, y, x + w - inset, y + thickness, color);
    }

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
