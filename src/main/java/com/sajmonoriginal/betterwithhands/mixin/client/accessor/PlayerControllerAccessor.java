package com.sajmonoriginal.betterwithhands.mixin.client.accessor;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.player.controller.PlayerController;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Environment(EnvType.CLIENT)
@Mixin(value = PlayerController.class, remap = false)
public interface PlayerControllerAccessor {
    @Invoker("syncCurrentPlayItem")
    void betterWithHands$syncCurrentPlayItem();
}
