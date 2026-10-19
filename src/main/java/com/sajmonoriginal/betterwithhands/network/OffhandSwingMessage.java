package com.sajmonoriginal.betterwithhands.network;

import com.sajmonoriginal.betterwithhands.util.OffhandCapable;
import com.sajmonoriginal.betterwithhands.util.OffhandScope;
import net.minecraft.core.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import turniplabs.halplibe.helper.network.NetworkMessage;
import turniplabs.halplibe.helper.network.UniversalPacket;

public class OffhandSwingMessage implements NetworkMessage {
    public OffhandSwingMessage() {
    }

    @Override
    public void encodeToUniversalPacket(@NotNull UniversalPacket packet) {
    }

    @Override
    public void decodeFromUniversalPacket(@NotNull UniversalPacket packet) {
    }

    @Override
    public void handle(NetworkContext context) {
        Player player = context.player;
        if (player.isAlive() && ((OffhandCapable) player.inventory).betterWithHands$getOffhandItem() != null) {
            OffhandScope.run(player, player::swingItem);
        }
    }
}
