package com.sajmonoriginal.betterwithhands;

import com.sajmonoriginal.betterwithhands.option.BetterWithHandsSettings;
import net.fabricmc.api.ClientModInitializer;
import turniplabs.halplibe.util.ClientStartEntrypoint;

public class BetterWithHandsClientMod implements ClientModInitializer, ClientStartEntrypoint {

    @Override
    public void onInitializeClient() {
        BetterWithHandsMod.LOGGER.info("Better With Hands client initialized");
    }

    @Override
    public void beforeClientStart() {
    }

    @Override
    public void afterClientStart() {
        BetterWithHandsSettings.init();
    }
}
