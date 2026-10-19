package com.sajmonoriginal.betterwithhands.network;

import com.sajmonoriginal.betterwithhands.util.OffhandCapable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jetbrains.annotations.NotNull;
import turniplabs.halplibe.helper.network.NetworkMessage;
import turniplabs.halplibe.helper.network.UniversalPacket;

public class SwapHandsMessage implements NetworkMessage {
    public SwapHandsMessage() {
    }

    @Override
    public void encodeToUniversalPacket(@NotNull UniversalPacket packet) {
    }

    @Override
    public void decodeFromUniversalPacket(@NotNull UniversalPacket packet) {
    }

    @Override
    @Environment(EnvType.SERVER)
    public void handleServerEnv(NetworkContext context) {
        ((OffhandCapable) context.player.inventory).betterWithHands$swapHands();
    }
}
