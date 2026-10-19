package com.sajmonoriginal.betterwithhands.mixin;

import com.sajmonoriginal.betterwithhands.mixin.accessor.MenuAbstractAccessor;
import com.sajmonoriginal.betterwithhands.slot.SlotOffhand;
import net.minecraft.core.player.inventory.container.ContainerInventory;
import net.minecraft.core.player.inventory.menu.MenuInventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = MenuInventory.class, remap = false)
public abstract class MenuInventoryMixin {
    @Inject(method = "<init>(Lnet/minecraft/core/player/inventory/container/ContainerInventory;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/player/inventory/menu/MenuInventory;slotsChanged(Lnet/minecraft/core/player/inventory/container/Container;)V"))
    private void addOffhandSlot(ContainerInventory inventory, CallbackInfo ci) {
        MenuInventory menu = (MenuInventory) (Object) this;
        ((MenuAbstractAccessor) menu).invokeAddSlot(new SlotOffhand(inventory, 152, 62));
    }
}
