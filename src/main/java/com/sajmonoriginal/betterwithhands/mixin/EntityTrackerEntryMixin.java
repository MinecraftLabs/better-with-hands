package com.sajmonoriginal.betterwithhands.mixin;

import com.sajmonoriginal.betterwithhands.util.OffhandCapable;
import com.sajmonoriginal.betterwithhands.util.OffhandSyncHelper;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.item.ItemStack;
import net.minecraft.server.entity.EntityTrackerEntryImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = EntityTrackerEntryImpl.class, remap = false)
public class EntityTrackerEntryMixin {

    @Shadow
    public Entity trackedEntity;

    @Inject(method = "updatePlayerEntity", at = @At("TAIL"))
    private void sendOffhandOnTrack(Player player, CallbackInfo ci) {
        if (this.trackedEntity instanceof Player) {
            Player trackedPlayer = (Player) this.trackedEntity;
            ItemStack offhand = ((OffhandCapable) trackedPlayer.inventory).betterWithHands$getOffhandItem();
            
            if (offhand != null) {
                OffhandSyncHelper.sendOffhandToPlayer(player, trackedPlayer.id, offhand);
            }
        }
    }
}
