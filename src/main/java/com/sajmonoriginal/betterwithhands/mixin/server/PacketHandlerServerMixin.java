package com.sajmonoriginal.betterwithhands.mixin.server;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.sajmonoriginal.betterwithhands.slot.SlotOffhand;
import com.sajmonoriginal.betterwithhands.util.HandAction;
import com.sajmonoriginal.betterwithhands.util.HandLogic;
import com.sajmonoriginal.betterwithhands.util.OffhandScope;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.net.packet.PacketUseOrPlaceItemStack;
import net.minecraft.core.player.inventory.container.Container;
import net.minecraft.core.player.inventory.menu.MenuAbstract;
import net.minecraft.core.player.inventory.slot.Slot;
import net.minecraft.server.entity.player.PlayerServer;
import net.minecraft.server.net.handler.PacketHandlerServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = PacketHandlerServer.class, remap = false)
public abstract class PacketHandlerServerMixin {
    @Shadow
    private PlayerServer playerEntity;

    @WrapMethod(method = "handleUseOrPlaceItem")
    private void useWithPreferredHand(PacketUseOrPlaceItemStack packet, Operation<Void> original) {
        HandLogic.perform(this.playerEntity, HandAction.USE, null, () -> {
            original.call(packet);
        });
    }

    @WrapOperation(
        method = "handleUseOrPlaceItem",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/core/player/inventory/menu/MenuAbstract;getSlotFor(Lnet/minecraft/core/player/inventory/container/Container;I)Lnet/minecraft/core/player/inventory/slot/Slot;")
    )
    private Slot resyncOffhandSlot(MenuAbstract menu, Container container, int index, Operation<Slot> original) {
        if (OffhandScope.isActive(this.playerEntity)) {
            SlotOffhand offhandSlot = SlotOffhand.find(menu);
            if (offhandSlot != null) {
                return offhandSlot;
            }
        }
        return original.call(menu, container, index);
    }

    @WrapOperation(
        method = "handleUseOrPlaceItem",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/core/item/ItemStack;areItemStacksEqual(Lnet/minecraft/core/item/ItemStack;Lnet/minecraft/core/item/ItemStack;)Z")
    )
    private boolean skipResyncWithoutOffhandSlot(ItemStack current, ItemStack sent, Operation<Boolean> original) {
        if (OffhandScope.isActive(this.playerEntity) && SlotOffhand.find(this.playerEntity.containerMenu) == null) {
            return true;
        }
        return original.call(current, sent);
    }
}
