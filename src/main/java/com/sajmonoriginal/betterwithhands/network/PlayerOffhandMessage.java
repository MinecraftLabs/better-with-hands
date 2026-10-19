package com.sajmonoriginal.betterwithhands.network;

import com.sajmonoriginal.betterwithhands.client.ClientHands;
import com.sajmonoriginal.betterwithhands.util.OffhandCapable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import turniplabs.halplibe.helper.network.NetworkMessage;
import turniplabs.halplibe.helper.network.UniversalPacket;

public class PlayerOffhandMessage implements NetworkMessage {
    private int playerId;
    private ItemStack offhand;

    public PlayerOffhandMessage() {
    }

    public PlayerOffhandMessage(int playerId, ItemStack offhand) {
        this.playerId = playerId;
        this.offhand = offhand;
    }

    @Override
    public void encodeToUniversalPacket(@NotNull UniversalPacket packet) {
        packet.writeInt(this.playerId);
        packet.writeBoolean(this.offhand != null);
        if (this.offhand != null) {
            packet.writeItemStack(this.offhand);
        }
    }

    @Override
    public void decodeFromUniversalPacket(@NotNull UniversalPacket packet) {
        this.playerId = packet.readInt();
        this.offhand = packet.readBoolean() ? packet.readItemStack() : null;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void handleClientEnv(NetworkContext context) {
        Player player = ClientHands.findRemotePlayer(this.playerId);
        if (player != null) {
            ((OffhandCapable) player.inventory).betterWithHands$setOffhandItem(this.offhand);
        }
    }
}
