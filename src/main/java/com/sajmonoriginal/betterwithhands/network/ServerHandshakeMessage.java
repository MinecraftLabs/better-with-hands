package com.sajmonoriginal.betterwithhands.network;

import com.sajmonoriginal.betterwithhands.client.ClientHands;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jetbrains.annotations.NotNull;
import turniplabs.halplibe.helper.network.NetworkMessage;
import turniplabs.halplibe.helper.network.UniversalPacket;

public class ServerHandshakeMessage implements NetworkMessage {
    public ServerHandshakeMessage() {
    }

    @Override
    public void encodeToUniversalPacket(@NotNull UniversalPacket packet) {
    }

    @Override
    public void decodeFromUniversalPacket(@NotNull UniversalPacket packet) {
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void handleClientEnv(NetworkContext context) {
        ClientHands.onServerHandshake();
    }
}
