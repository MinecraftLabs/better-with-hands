package com.sajmonoriginal.betterwithhands.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.core.player.inventory.container.ContainerInventory;
import net.minecraft.core.player.inventory.menu.MenuAbstract;
import net.minecraft.core.player.inventory.menu.MenuInventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value = MenuAbstract.class, remap = false)
public abstract class MenuAbstractMixin {
    @Unique
    private static final int HOTBAR_SLOTS_START = 5 + ContainerInventory.ARMOR_INVENTORY_SIZE + 27;

    @WrapMethod(method = "getHotbarSlotId")
    private int betterWithHands$getHotbarSlotId(int number, Operation<Integer> original) {
        if ((Object) this instanceof MenuInventory) {
            return HOTBAR_SLOTS_START + number - 1;
        }
        return original.call(number);
    }
}
