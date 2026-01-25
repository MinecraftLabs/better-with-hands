package com.sajmonoriginal.betterwithhands.mixin;

import com.sajmonoriginal.betterwithhands.util.OffhandCapable;
import com.sajmonoriginal.betterwithhands.util.OffhandSyncHelper;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.world.World;
import net.minecraft.server.entity.player.PlayerServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = PlayerServer.class, remap = false)
public abstract class PlayerServerMixin extends Player {

    @Unique
    private ItemStack betterWithHands$lastOffhand = null;
    
    @Unique
    private int betterWithHands$lastOffhandId = -1;
    
    @Unique
    private int betterWithHands$lastOffhandMeta = -1;
    
    @Unique
    private int betterWithHands$lastOffhandCount = -1;

    public PlayerServerMixin(World world) {
        super(world);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void checkAndBroadcastOffhandChanges(CallbackInfo ci) {
        ItemStack currentOffhand = ((OffhandCapable) this.inventory).betterWithHands$getOffhandItem();
        
        boolean changed = false;
        
        if (currentOffhand == null && this.betterWithHands$lastOffhand != null) {
            changed = true;
        } else if (currentOffhand != null && this.betterWithHands$lastOffhand == null) {
            changed = true;
        } else if (currentOffhand != null) {
            if (currentOffhand.itemID != this.betterWithHands$lastOffhandId ||
                currentOffhand.getMetadata() != this.betterWithHands$lastOffhandMeta ||
                currentOffhand.stackSize != this.betterWithHands$lastOffhandCount) {
                changed = true;
            }
        }
        
        if (changed) {
            OffhandSyncHelper.broadcastOffhandToNearbyPlayers(this, currentOffhand);
            OffhandSyncHelper.syncOffhandToOwner(this, currentOffhand);
            
            if (currentOffhand != null) {
                this.betterWithHands$lastOffhand = currentOffhand.copy();
                this.betterWithHands$lastOffhandId = currentOffhand.itemID;
                this.betterWithHands$lastOffhandMeta = currentOffhand.getMetadata();
                this.betterWithHands$lastOffhandCount = currentOffhand.stackSize;
            } else {
                this.betterWithHands$lastOffhand = null;
                this.betterWithHands$lastOffhandId = -1;
                this.betterWithHands$lastOffhandMeta = -1;
                this.betterWithHands$lastOffhandCount = -1;
            }
        }
    }
}
