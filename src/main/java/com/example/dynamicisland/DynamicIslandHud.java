package com.example.dynamicisland;

import net.minecraft.client.Minecraft;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;

public class DynamicIslandHud {

    private static final Identifier ISLAND_TEXTURE =
            Identifier.fromNamespaceAndPath("dynamic-island", "textures/gui/island.png");

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

        float speed = 8f;
        progress += (target - progress) * Math.min(1f, dt * speed);
        if (Math.abs(target - progress) < 0.005f) progress = target;

        float eased = easeOutBack(progress);

        int screenWidth = client.getWindow().getGuiScaledWidth();

        int baseWidth     = 44,  expandedWidth     = 200;
        int baseHeight    = 10,  expandedHeight    = 120;

        int width  = (int) (baseWidth  + (expandedWidth  - baseWidth)  * eased);
        int height = (int) (baseHeight + (expandedHeight - baseHeight) * eased);

        int x = (screenWidth - width) / 2;

        int baseY = 8;
        int expandedY = 30;
        int y = (int) (baseY + (expandedY - baseY) * eased);

        graphics.blit(
            RenderPipelines.GUI_TEXTURED,
            ISLAND_TEXTURE,
            x, y,
            0, 0,
            width, height,
            2688, 1536
        );

        if (eased < 0.5f) {
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
    }

    private static float easeOutBack(float t) {
        float c1 = 1.70158f;
        float c3 = c1 + 1f;
        float p = t - 1f;
        return 1f + c3 * p * p * p + c1 * p * p;
    }
}