package com.example.dynamicisland.mixin;

import com.example.dynamicisland.DynamicIslandHud;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;
import org.joml.Matrix3x2fStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
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
}
