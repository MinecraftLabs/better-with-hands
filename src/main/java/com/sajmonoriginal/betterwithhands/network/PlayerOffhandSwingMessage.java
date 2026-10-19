package com.sajmonoriginal.betterwithhands.network;

import com.sajmonoriginal.betterwithhands.client.ClientHands;
import com.sajmonoriginal.betterwithhands.util.OffhandSwingCapable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import turniplabs.halplibe.helper.network.NetworkMessage;
import turniplabs.halplibe.helper.network.UniversalPacket;

public class PlayerOffhandSwingMessage implements NetworkMessage {
    private int playerId;

    public PlayerOffhandSwingMessage() {
    }

    public PlayerOffhandSwingMessage(int playerId) {
        this.playerId = playerId;
    }

    @Override
    public void encodeToUniversalPacket(@NotNull UniversalPacket packet) {
        packet.writeInt(this.playerId);
    }

    @Override
    public void decodeFromUniversalPacket(@NotNull UniversalPacket packet) {
        this.playerId = packet.readInt();
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void handleClientEnv(NetworkContext context) {
        Player player = ClientHands.findRemotePlayer(this.playerId);
        if (player != null) {
            ((OffhandSwingCapable) player).betterWithHands$swingOffhand();
        }
    }
}
