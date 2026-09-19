package com.example.dynamicisland.mixin;

import com.example.dynamicisland.DynamicIslandHud;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerTabOverlay.class)
public class PlayerTabOverlayMixin {

    // 保留原来的：监听 Tab 显示状态变化
    @Inject(method = "setVisible", at = @At("HEAD"), require = 1)
    private void dynamicIsland$onSetVisible(boolean visible, CallbackInfo ci) {
        DynamicIslandHud.setTarget(visible ? 1f : 0f);
    }

    // 新增：在 render 方法开头做缩放和淡入
    @Inject(method = "render", at = @At("HEAD"), require = 1)
    private void dynamicIsland$onRenderHead(
            net.minecraft.client.gui.GuiGraphicsExtractor graphics,
            int screenWidth,
            CallbackInfo ci) {
        // 获取动画进度
        float progress = DynamicIslandHud.getProgress();

        // 完全收起时不渲染（节省性能）
        if (progress <= 0.01f) {
            ci.cancel();  // 取消原方法执行，列表不显示
            return;
        }

        // 用 PoseStack 做缩放变换
        // 注意：GuiGraphicsExtractor 的具体 API 可能需要调整
        var pose = graphics.pose();
        pose.pushPose();

        // 以屏幕中心为锚点缩放
        float scale = 0.8f + 0.2f * progress;  // 0.8 → 1.0
        pose.translate(screenWidth / 2.0, 0, 0);
        pose.scale(scale, scale, 1.0f);
        pose.translate(-screenWidth / 2.0, 0, 0);

        // 透明度通过 mixin 返回值处理比较麻烦，
        // 更简单的方式是让 DynamicIslandHud 暴露一个 alpha 值，
        // 然后在 render 方法内部通过 @Redirect 修改颜色
    }

    // 在 render 方法返回时恢复变换
    @Inject(method = "render", at = @At("RETURN"), require = 1)
    private void dynamicIsland$onRenderReturn(
            net.minecraft.client.gui.GuiGraphicsExtractor graphics,
            int screenWidth,
            CallbackInfo ci) {
        if (DynamicIslandHud.getProgress() > 0.01f) {
            graphics.pose().popPose();
        }
    }
}
