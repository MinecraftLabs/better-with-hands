package com.sajmonoriginal.betterwithhands.mixin.server;

import com.sajmonoriginal.betterwithhands.network.PlayerOffhandMessage;
import com.sajmonoriginal.betterwithhands.network.PlayerOffhandSwingMessage;
import com.sajmonoriginal.betterwithhands.util.OffhandCapable;
import com.sajmonoriginal.betterwithhands.util.OffhandScope;
import com.sajmonoriginal.betterwithhands.util.OffhandSwingCapable;
import net.minecraft.core.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.entity.player.PlayerServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import turniplabs.halplibe.helper.network.NetworkHandler;
import turniplabs.halplibe.helper.network.NetworkMessage;

@Mixin(value = PlayerServer.class, remap = false)
public abstract class PlayerServerMixin {
    @Shadow
    public MinecraftServer mcServer;

    @Unique
    private ItemStack betterWithHands$trackedOffhand = null;

    @Inject(method = "tick", at = @At("TAIL"))
    private void broadcastOffhandChange(CallbackInfo ci) {
        PlayerServer self = (PlayerServer) (Object) this;
        ItemStack offhand = ((OffhandCapable) self.inventory).betterWithHands$getOffhandItem();
        if (ItemStack.areItemStacksEqual(offhand, this.betterWithHands$trackedOffhand)) {
            return;
        }
        this.betterWithHands$trackedOffhand = ItemStack.copyItemStack(offhand);
        PlayerOffhandMessage message = new PlayerOffhandMessage(self.id, offhand);
        this.betterWithHands$sendToTrackers(message);
        if (self.containerMenu != self.inventoryMenu) {
            NetworkHandler.sendToPlayer(self, message);
        }
    }

    @Inject(method = "swingItem", at = @At("HEAD"), cancellable = true)
    private void swingOffhandInScope(CallbackInfo ci) {
        PlayerServer self = (PlayerServer) (Object) this;
        if (!OffhandScope.isActive(self)) {
            return;
        }
        OffhandSwingCapable swing = (OffhandSwingCapable) self;
        if (!swing.betterWithHands$isSwingingOffhand()) {
            swing.betterWithHands$swingOffhand();
            this.betterWithHands$sendToTrackers(new PlayerOffhandSwingMessage(self.id));
        }
        ci.cancel();
    }

    @Unique
    private void betterWithHands$sendToTrackers(NetworkMessage message) {
        PlayerServer self = (PlayerServer) (Object) this;
        this.mcServer.getEntityTracker(self.dimension)
            .sendPacketToTrackedPlayers(self, NetworkHandler.generateCompatibilityNetworkMessagePacket(message));
    }
}
