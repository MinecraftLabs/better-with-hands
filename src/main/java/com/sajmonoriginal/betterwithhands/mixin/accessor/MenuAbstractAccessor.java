package com.sajmonoriginal.betterwithhands.mixin.accessor;

import net.minecraft.core.player.inventory.menu.MenuAbstract;
import net.minecraft.core.player.inventory.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value = MenuAbstract.class, remap = false)
public interface MenuAbstractAccessor {
    @Invoker
    void invokeAddSlot(Slot slot);
}
