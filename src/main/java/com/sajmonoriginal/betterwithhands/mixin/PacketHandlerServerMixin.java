package com.sajmonoriginal.betterwithhands.mixin;

import com.sajmonoriginal.betterwithhands.util.DynamicPriorityHelper;
import com.sajmonoriginal.betterwithhands.util.OffhandCapable;
import net.minecraft.core.item.ItemStack;
import net.minecraft.server.entity.player.PlayerServer;
import net.minecraft.server.net.handler.PacketHandlerServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(value = PacketHandlerServer.class, remap = false)
public class PacketHandlerServerMixin {
    @Shadow
    private PlayerServer playerEntity;

    @ModifyVariable(method = "handlePlace", at = @At("STORE"), ordinal = 0)
    private ItemStack modifyItemStack(ItemStack original) {
        OffhandCapable offhandInventory = (OffhandCapable) this.playerEntity.inventory;
        ItemStack offhand = offhandInventory.betterWithHands$getOffhandItem();
        
        if (offhand == null) {
            return original;
        }
        
        boolean dynamicPriority = offhandInventory.betterWithHands$getDynamicPriority();
        if (dynamicPriority) {
            boolean useOffhand = DynamicPriorityHelper.shouldUseOffhandForAction(
                original, offhand, DynamicPriorityHelper.ActionType.USE, null);
            if (useOffhand) {
                return offhand;
            }
            return original;
        }
        
        boolean swapPriority = offhandInventory.betterWithHands$getSwapPriority();
        boolean useWhenEmpty = offhandInventory.betterWithHands$getUseWhenEmpty();
        
        if (swapPriority) {
            return offhand;
        } else if (useWhenEmpty && original == null) {
            return offhand;
        }
        
        return original;
    }
}
