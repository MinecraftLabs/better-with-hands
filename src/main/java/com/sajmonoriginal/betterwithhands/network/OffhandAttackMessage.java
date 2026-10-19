package com.sajmonoriginal.betterwithhands.network;

import com.sajmonoriginal.betterwithhands.util.OffhandScope;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.net.packet.PacketEntityInteract;
import net.minecraft.server.entity.player.PlayerServer;
import org.jetbrains.annotations.NotNull;
import turniplabs.halplibe.helper.network.NetworkMessage;
import turniplabs.halplibe.helper.network.UniversalPacket;

public class OffhandAttackMessage implements NetworkMessage {
    private static final int ATTACK_ACTION = 1;

    private int targetId;

    public OffhandAttackMessage() {
    }

    public OffhandAttackMessage(int targetId) {
        this.targetId = targetId;
    }

    @Override
    public void encodeToUniversalPacket(@NotNull UniversalPacket packet) {
        packet.writeInt(this.targetId);
    }

    @Override
    public void decodeFromUniversalPacket(@NotNull UniversalPacket packet) {
        this.targetId = packet.readInt();
    }

    @Override
    @Environment(EnvType.SERVER)
    public void handleServerEnv(NetworkContext context) {
        if (context.player instanceof PlayerServer player) {
            PacketEntityInteract attack = new PacketEntityInteract(player.id, this.targetId, ATTACK_ACTION);
            OffhandScope.run(player, () -> player.playerNetServerHandler.handleEntityInteract(attack));
        }
    }
}
