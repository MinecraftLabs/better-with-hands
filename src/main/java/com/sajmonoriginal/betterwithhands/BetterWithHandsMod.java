package com.sajmonoriginal.betterwithhands;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BetterWithHandsMod implements ModInitializer {
    public static final String MOD_ID = "better_with_hands";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    
    public static final int OFFHAND_SLOT = 40;

    @Override
    public void onInitialize() {
        LOGGER.info("Better With Hands initialized - Off-hand slot enabled!");
    }
}
