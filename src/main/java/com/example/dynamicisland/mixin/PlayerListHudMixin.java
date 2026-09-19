package com.example.dynamicisland.mixin;

import com.example.dynamicisland.DynamicIslandHud;
import net.minecraft.client.gui.hud.PlayerListHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerListHud.class)
public class PlayerListHudMixin {

    @Inject(method = "setVisible", at = @At("HEAD"), require = 1)
    private void dynamicIsland$onSetVisible(boolean visible, CallbackInfo ci) {
        DynamicIslandHud.setTarget(visible ? 1f : 0f);
    }
}