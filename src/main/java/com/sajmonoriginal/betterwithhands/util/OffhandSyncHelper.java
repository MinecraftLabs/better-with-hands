package com.sajmonoriginal.betterwithhands.util;

import com.sajmonoriginal.betterwithhands.BetterWithHandsMod;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.net.packet.PacketCustomPayload;
import net.minecraft.server.MinecraftServer;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

public class OffhandSyncHelper {
    
    private static final double SYNC_RADIUS = 64.0;

    private OffhandSyncHelper() {
    }

    public static void broadcastOffhandToNearbyPlayers(Player player, ItemStack offhandItem) {
        MinecraftServer server = MinecraftServer.getInstance();
        if (server == null || server.playerList == null) {
            return;
        }

        try {
            PacketCustomPayload packet = createOffhandSyncPacket(player.id, offhandItem);
            server.playerList.sendPacketToOtherPlayersAroundPoint(
                player,
                player.x, player.y, player.z,
                SYNC_RADIUS,
                player.world.dimension.id,
                packet
            );
        } catch (Exception e) {
            BetterWithHandsMod.LOGGER.error("Failed to broadcast offhand sync", e);
        }
    }

    public static void syncOffhandToOwner(Player player, ItemStack offhandItem) {
        MinecraftServer server = MinecraftServer.getInstance();
        if (server == null || server.playerList == null) {
            return;
        }

        try {
            PacketCustomPayload packet = createOffhandSyncPacket(player.id, offhandItem);
            server.playerList.sendPacketToPlayer(player.username, packet);
        } catch (Exception e) {
            BetterWithHandsMod.LOGGER.error("Failed to sync offhand to owner", e);
        }
    }

    public static void sendOffhandToPlayer(Player targetPlayer, int sourceEntityId, ItemStack offhandItem) {
        MinecraftServer server = MinecraftServer.getInstance();
        if (server == null || server.playerList == null) {
            return;
        }

        try {
            PacketCustomPayload packet = createOffhandSyncPacket(sourceEntityId, offhandItem);
            server.playerList.sendPacketToPlayer(targetPlayer.username, packet);
        } catch (Exception e) {
            BetterWithHandsMod.LOGGER.error("Failed to send offhand to player", e);
        }
    }

    public static PacketCustomPayload createOffhandSyncPacket(int entityId, ItemStack offhandItem) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        
        dos.writeByte(2);
        dos.writeInt(entityId);
        dos.writeBoolean(offhandItem != null);
        
        if (offhandItem != null) {
            dos.writeInt(offhandItem.itemID);
            dos.writeByte(offhandItem.stackSize);
            dos.writeInt(offhandItem.getMetadata());
        }
        
        return new PacketCustomPayload(BetterWithHandsMod.MOD_ID, baos.toByteArray());
    }
}
