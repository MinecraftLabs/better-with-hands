package com.sajmonoriginal.betterwithhands;

import com.sajmonoriginal.betterwithhands.gui.HudComponentOffhand;
import com.sajmonoriginal.betterwithhands.option.BetterWithHandsSettings;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.hud.component.HudComponents;
import turniplabs.halplibe.event.defs.ClientEvents;
import turniplabs.halplibe.util.dependency.Key;

public class BetterWithHandsClientMod implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        Key key = Key.of(BetterWithHandsMod.MOD_ID);
        ClientEvents.AFTER_CLIENT_START.listen(key, BetterWithHandsSettings::init);
        BetterWithHandsMod.LOGGER.info("Better With Hands client initialized");
    }

    public static void registerHudComponents() {
        HudComponents.register(
            new HudComponentOffhand("better_with_hands.offhand_slot", HudComponentOffhand.defaultLayout(false))
        );
    }
}
