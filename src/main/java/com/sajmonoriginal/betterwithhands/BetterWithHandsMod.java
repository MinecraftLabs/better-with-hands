package com.sajmonoriginal.betterwithhands;

import net.fabricmc.api.ModInitializer;
import net.minecraft.core.lang.Language;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import turniplabs.halplibe.HalpLibe;

public class BetterWithHandsMod implements ModInitializer {

    public static final String MOD_ID = HalpLibe.registerMod("better_with_hands", true);
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    static {

        Language.Default.INSTANCE.loadNamespace(MOD_ID);
    }

    public static final int OFFHAND_SLOT = 40;

    @Override
    public void onInitialize() {
        LOGGER.info("Better With Hands initialized - Off-hand slot enabled!");
    }
}
