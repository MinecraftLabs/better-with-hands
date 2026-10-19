package com.sajmonoriginal.betterwithhands.mixin.server;

import com.sajmonoriginal.betterwithhands.network.PlayerOffhandMessage;
import com.sajmonoriginal.betterwithhands.util.OffhandCapable;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.item.ItemStack;
import net.minecraft.server.entity.EntityTrackerEntryImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import turniplabs.halplibe.helper.network.NetworkHandler;

@Mixin(value = EntityTrackerEntryImpl.class, remap = false)
public abstract class EntityTrackerEntryImplMixin {
    @Shadow
    public Entity trackedEntity;

    @Inject(
        method = "updatePlayerEntity",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/core/entity/Entity;getInventory()[Lnet/minecraft/core/item/ItemStack;")
    )
    private void sendOffhandToNewViewer(Player viewer, CallbackInfo ci) {
        if (this.trackedEntity instanceof Player trackedPlayer) {
            ItemStack offhand = ((OffhandCapable) trackedPlayer.inventory).betterWithHands$getOffhandItem();
            if (offhand != null) {
                NetworkHandler.sendToPlayer(viewer, new PlayerOffhandMessage(trackedPlayer.id, offhand));
            }
        }
    }
}
