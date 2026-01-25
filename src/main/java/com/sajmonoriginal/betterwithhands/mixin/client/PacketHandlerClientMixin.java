package com.sajmonoriginal.betterwithhands.mixin.client;

import com.sajmonoriginal.betterwithhands.BetterWithHandsMod;
import com.sajmonoriginal.betterwithhands.util.OffhandCapable;
import com.sajmonoriginal.betterwithhands.util.OffhandSwingCapable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.net.handler.PacketHandlerClient;
import net.minecraft.client.world.WorldClientMP;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.net.packet.PacketCustomPayload;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;

@Environment(EnvType.CLIENT)
@Mixin(value = PacketHandlerClient.class, remap = false)
public class PacketHandlerClientMixin {
    @Final
    @Shadow
    private Minecraft mc;

    @Inject(method = "handleCustomPayload", at = @At("HEAD"), cancellable = true)
    private void handleOffhandSync(PacketCustomPayload packet, CallbackInfo ci) {
        if (!BetterWithHandsMod.MOD_ID.equals(packet.channel)) {
            return;
        }

        try {
            DataInputStream dis = new DataInputStream(new ByteArrayInputStream(packet.data));
            byte action = dis.readByte();

            if (action == 2) {
                int entityId = dis.readInt();
                boolean hasItem = dis.readBoolean();
                
                ItemStack offhandItem = null;
                if (hasItem) {
                    int itemId = dis.readInt();
                    int stackSize = dis.readByte();
                    int metadata = dis.readInt();
                    offhandItem = new ItemStack(itemId, stackSize, metadata);
                }

                if (this.mc.currentWorld != null && this.mc.currentWorld instanceof WorldClientMP) {
                    Entity entity;
                    if (entityId == this.mc.thePlayer.id) {
                        entity = this.mc.thePlayer;
                    } else {
                        entity = ((WorldClientMP) this.mc.currentWorld).getEntityFromId(entityId);
                    }
                    if (entity instanceof Player) {
                        Player player = (Player) entity;
                        ((OffhandCapable) player.inventory).betterWithHands$setOffhandItem(offhandItem);
                    }
                }
                
                ci.cancel();
            } else if (action == 3) {
                int entityId = dis.readInt();
                
                if (this.mc.currentWorld != null && this.mc.currentWorld instanceof WorldClientMP) {
                    Entity entity = ((WorldClientMP) this.mc.currentWorld).getEntityFromId(entityId);
                    if (entity instanceof Player && entity != this.mc.thePlayer) {
                        ((OffhandSwingCapable) entity).betterWithHands$swingOffhand();
                    }
                }
                
                ci.cancel();
            }
        } catch (Exception e) {
            BetterWithHandsMod.LOGGER.error("Error handling offhand sync packet", e);
        }
    }
}
