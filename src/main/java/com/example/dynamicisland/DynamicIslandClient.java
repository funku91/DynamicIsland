package com.example.dynamicisland;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.resources.Identifier;

public class DynamicIslandClient implements ClientModInitializer {

    public static final String MOD_ID = "dynamic-island";

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    @Override
    public void onInitializeClient() {
        HudElementRegistry.attachElementBefore(
                VanillaHudElements.CHAT,
                id("dynamic_island"),
                DynamicIslandHud::render
        );
    }
}
