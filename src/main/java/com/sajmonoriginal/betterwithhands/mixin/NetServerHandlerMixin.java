package com.sajmonoriginal.betterwithhands.mixin;

import com.sajmonoriginal.betterwithhands.BetterWithHandsMod;
import com.sajmonoriginal.betterwithhands.util.OffhandCapable;
import net.minecraft.core.net.packet.PacketCustomPayload;
import net.minecraft.server.entity.player.PlayerServer;
import net.minecraft.server.net.handler.PacketHandlerServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;

@Mixin(value = PacketHandlerServer.class, remap = false)
public class NetServerHandlerMixin {
    @Shadow
    private PlayerServer playerEntity;

    @Inject(method = "handleCustomPayload", at = @At("HEAD"), cancellable = true)
    private void handleOffhandSwap(PacketCustomPayload packet, CallbackInfo ci) {
        if (BetterWithHandsMod.MOD_ID.equals(packet.channel)) {
            try {
                DataInputStream dis = new DataInputStream(new ByteArrayInputStream(packet.data));
                byte action = dis.readByte();
                
                if (action == 0) {
                    ((OffhandCapable) this.playerEntity.inventory).betterWithHands$swapHands();
                } else if (action == 1) {
                    boolean swapPriority = dis.readBoolean();
                    boolean useWhenEmpty = dis.readBoolean();
                    boolean dynamicPriority = dis.available() > 0 ? dis.readBoolean() : false;
                    OffhandCapable inventory = (OffhandCapable) this.playerEntity.inventory;
                    inventory.betterWithHands$setSwapPriority(swapPriority);
                    inventory.betterWithHands$setUseWhenEmpty(useWhenEmpty);
                    inventory.betterWithHands$setDynamicPriority(dynamicPriority);
                }
                
                ci.cancel();
            } catch (Exception e) {
                BetterWithHandsMod.LOGGER.error("Error handling offhand packet", e);
            }
        }
    }
}
