package com.example.dynamicisland.mixin;

import com.example.dynamicisland.DynamicIslandHud;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(PlayerTabOverlay.class)
public class PlayerTabOverlayMixin {

    @Inject(method = "setVisible", at = @At("HEAD"), require = 1)
    private void dynamicIsland$onSetVisible(boolean visible, CallbackInfo ci) {
        DynamicIslandHud.setTarget(visible ? 1f : 0f);
    }

    /**
     * 去掉 header/footer 的大块黑色填充（Integer.MIN_VALUE = 0x80000000）。
     * 原版在 extractRenderState 中对 header 和 footer 区域绘制半透明黑矩形，
     * 去掉后灵动岛不再被大面积黑块遮挡。
     */
    @ModifyConstant(
        method = "extractRenderState",
        constant = @Constant(intValue = Integer.MIN_VALUE)
    )
    private int dynamicIsland$transparentHeaderFooter(int original) {
        return 0x00000000;
    }

    /**
     * 去掉每行玩家条目的背景黑条。
     * 源码：int background = this.minecraft.options.getBackgroundColor(553648127);
     * 返回 0 使 fill(xo, yo, xo+slotWidth, yo+8, background) 成为空操作。
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
