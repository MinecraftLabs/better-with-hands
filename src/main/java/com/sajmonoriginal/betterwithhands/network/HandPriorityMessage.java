package com.sajmonoriginal.betterwithhands.network;

import com.sajmonoriginal.betterwithhands.util.HandPriority;
import com.sajmonoriginal.betterwithhands.util.OffhandCapable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jetbrains.annotations.NotNull;
import turniplabs.halplibe.helper.network.NetworkMessage;
import turniplabs.halplibe.helper.network.UniversalPacket;

public class HandPriorityMessage implements NetworkMessage {
    private HandPriority priority = HandPriority.DEFAULT;

    public HandPriorityMessage() {
    }

    public HandPriorityMessage(HandPriority priority) {
        this.priority = priority;
    }

    @Override
    public void encodeToUniversalPacket(@NotNull UniversalPacket packet) {
        packet.writeBoolean(this.priority.swapPriority());
        packet.writeBoolean(this.priority.useWhenEmpty());
        packet.writeBoolean(this.priority.dynamic());
    }

    @Override
    public void decodeFromUniversalPacket(@NotNull UniversalPacket packet) {
        this.priority = new HandPriority(packet.readBoolean(), packet.readBoolean(), packet.readBoolean());
    }

    @Override
    @Environment(EnvType.SERVER)
    public void handleServerEnv(NetworkContext context) {
        ((OffhandCapable) context.player.inventory).betterWithHands$setHandPriority(this.priority);
    }
}
