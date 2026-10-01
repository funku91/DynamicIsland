package com.example.dynamicisland.mixin;

import com.example.dynamicisland.DynamicIslandHud;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;
import org.joml.Matrix3x2fStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerTabOverlay.class)
public class PlayerTabOverlayMixin {

    @Inject(method = "setVisible", at = @At("HEAD"), require = 1)
    private void dynamicIsland$onSetVisible(boolean visible, CallbackInfo ci) {
        DynamicIslandHud.setTarget(visible ? 1f : 0f);
    }

    @Inject(method = "extractRenderState", at = @At("HEAD"), require = 1, cancellable = true)
    private void dynamicIsland$onRenderHead(
            GuiGraphicsExtractor graphics,
            int screenWidth,
            Scoreboard scoreboard,
            Objective displayObjective,
            CallbackInfo ci) {
        float progress = DynamicIslandHud.getProgress();

        if (progress <= 0.01f) {
            ci.cancel();
            return;
        }

        Matrix3x2fStack pose = graphics.pose();
        pose.pushMatrix();

        float scale = 0.8f + 0.2f * progress;
        float centerX = screenWidth / 2.0f;

        pose.translate(centerX, 0.0f);
        pose.scale(scale, scale);
        pose.translate(-centerX, 0.0f);
    }

    @Inject(method = "extractRenderState", at = @At("RETURN"), require = 1)
    private void dynamicIsland$onRenderReturn(
            GuiGraphicsExtractor graphics,
            int screenWidth,
            Scoreboard scoreboard,
            Objective displayObjective,
            CallbackInfo ci) {
        if (DynamicIslandHud.getProgress() > 0.01f) {
            graphics.pose().popMatrix();
        }
    }

    /**
     * 拦截大黑框（表头/主体/表尾）。
     * 这些 fill 的颜色固定是 Integer.MIN_VALUE。
     */
    @Redirect(
        method = "extractRenderState",
        at = @At(value = "INVOKE",
                 target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;fill(IIIII)V")
    )
    private void dynamicIsland$skipBigBackground(GuiGraphicsExtractor graphics,
                                                  int x1, int y1, int x2, int y2,
                                                  int color) {
        if (color == Integer.MIN_VALUE) {
            return;
        }
        graphics.fill(x1, y1, x2, y2, color);
    }

    /**
     * 拦截每行的黑条。
     * 源码：int background = this.minecraft.options.getBackgroundColor(553648127);
     * 直接让它返回 0（完全透明），每行 fill 就是空操作。
     */
    @Redirect(
        method = "extractRenderState",
        at = @At(value = "INVOKE",
                 target = "Lnet/minecraft/client/Options;getBackgroundColor(I)I")
    )
    private int dynamicIsland$noRowBackground(Options options, int defaultColor) {
        return 0;
    }
}
