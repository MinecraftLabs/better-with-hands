package com.sajmonoriginal.betterwithhands.mixin;

import com.sajmonoriginal.betterwithhands.BetterWithHandsMod;
import com.sajmonoriginal.betterwithhands.util.OffhandCapable;
import com.sajmonoriginal.betterwithhands.util.OffhandSwingCapable;
import com.sajmonoriginal.betterwithhands.util.OffhandSyncHelper;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.net.packet.PacketCustomPayload;
import net.minecraft.core.util.helper.Side;
import net.minecraft.core.world.World;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.entity.player.PlayerServer;
import net.minecraft.server.net.handler.PacketHandlerServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

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
                } else if (action == 3) {
                    ((OffhandSwingCapable) this.playerEntity).betterWithHands$swingOffhand();
                    broadcastOffhandSwing();
                } else if (action == 4) {
                    int blockX = dis.readInt();
                    int blockY = dis.readInt();
                    int blockZ = dis.readInt();
                    int sideId = dis.readInt();
                    double xPlaced = dis.readDouble();
                    double yPlaced = dis.readDouble();

                    handleUseOffhand(blockX, blockY, blockZ, Side.fromId(sideId), xPlaced, yPlaced);
                }

                ci.cancel();
            } catch (Exception e) {
                BetterWithHandsMod.LOGGER.error("Error handling offhand packet", e);
            }
        }
    }

    private void broadcastOffhandSwing() {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            dos.writeByte(3);
            dos.writeInt(this.playerEntity.id);

            PacketCustomPayload swingPacket = new PacketCustomPayload(BetterWithHandsMod.MOD_ID, baos.toByteArray());

            MinecraftServer server = MinecraftServer.getInstance();
            if (server != null && server.playerList != null) {
                server.playerList.sendPacketToOtherPlayersAroundPoint(
                    this.playerEntity,
                    this.playerEntity.x, this.playerEntity.y, this.playerEntity.z,
                    64.0,
                    this.playerEntity.world.dimension.id,
                    swingPacket
                );
            }
        } catch (Exception e) {
            BetterWithHandsMod.LOGGER.error("Error broadcasting offhand swing", e);
        }
    }

    private void handleUseOffhand(int blockX, int blockY, int blockZ, Side side, double xPlaced, double yPlaced) {
        OffhandCapable offhandInventory = (OffhandCapable) this.playerEntity.inventory;
        ItemStack offhand = offhandInventory.betterWithHands$getOffhandItem();

        if (offhand == null) {
            return;
        }

        World world = this.playerEntity.world;

        offhandInventory.betterWithHands$setUsingOffhand(true);
        try {
            boolean used = offhand.useItem(this.playerEntity, world, blockX, blockY, blockZ, side, xPlaced, yPlaced);

            if (used) {
                if (offhand.stackSize <= 0) {
                    offhandInventory.betterWithHands$setOffhandItem(null);
                }
                OffhandSyncHelper.syncOffhandToOwner(this.playerEntity, offhandInventory.betterWithHands$getOffhandItem());
            }
        } finally {
            offhandInventory.betterWithHands$setUsingOffhand(false);
        }
    }
}
