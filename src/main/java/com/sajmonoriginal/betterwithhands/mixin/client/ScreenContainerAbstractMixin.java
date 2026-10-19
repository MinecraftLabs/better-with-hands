package com.sajmonoriginal.betterwithhands.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.sajmonoriginal.betterwithhands.slot.SlotOffhand;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.container.ScreenContainerAbstract;
import net.minecraft.core.player.inventory.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Environment(EnvType.CLIENT)
@Mixin(value = ScreenContainerAbstract.class, remap = false)
public abstract class ScreenContainerAbstractMixin {
    @ModifyReturnValue(method = "getSlotAtPosition", at = @At("RETURN"))
    private Slot skipDisabledOffhandSlot(Slot slot) {
        return slot instanceof SlotOffhand offhandSlot && !offhandSlot.isEnabled() ? null : slot;
    }
}
