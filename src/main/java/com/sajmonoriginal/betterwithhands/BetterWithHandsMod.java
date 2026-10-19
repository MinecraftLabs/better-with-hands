package com.sajmonoriginal.betterwithhands;

import com.sajmonoriginal.betterwithhands.network.HandPriorityMessage;
import com.sajmonoriginal.betterwithhands.network.OffhandAttackMessage;
import com.sajmonoriginal.betterwithhands.network.OffhandSwingMessage;
import com.sajmonoriginal.betterwithhands.network.PlayerOffhandMessage;
import com.sajmonoriginal.betterwithhands.network.PlayerOffhandSwingMessage;
import com.sajmonoriginal.betterwithhands.network.ServerHandshakeMessage;
import com.sajmonoriginal.betterwithhands.network.SwapHandsMessage;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import turniplabs.halplibe.HalpLibe;
import turniplabs.halplibe.helper.network.NetworkHandler;

public class BetterWithHandsMod implements ModInitializer {
    public static final String MOD_ID = HalpLibe.registerMod("better_with_hands", true);
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        NetworkHandler.registerNetworkMessage(ServerHandshakeMessage::new);
        NetworkHandler.registerNetworkMessage(HandPriorityMessage::new);
        NetworkHandler.registerNetworkMessage(SwapHandsMessage::new);
        NetworkHandler.registerNetworkMessage(OffhandSwingMessage::new);
        NetworkHandler.registerNetworkMessage(OffhandAttackMessage::new);
        NetworkHandler.registerNetworkMessage(PlayerOffhandMessage::new);
        NetworkHandler.registerNetworkMessage(PlayerOffhandSwingMessage::new);
        LOGGER.info("Better With Hands initialized");
    }
}
