package com.sajmonoriginal.betterwithhands.mixin.client;

import com.sajmonoriginal.betterwithhands.network.OffhandSwingMessage;
import com.sajmonoriginal.betterwithhands.util.OffhandScope;
import com.sajmonoriginal.betterwithhands.util.OffhandSwingCapable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.entity.player.PlayerLocalMultiplayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import turniplabs.halplibe.helper.network.NetworkHandler;

@Environment(EnvType.CLIENT)
@Mixin(value = PlayerLocalMultiplayer.class, remap = false)
public abstract class PlayerLocalMultiplayerMixin {
    @Inject(method = "swingItem", at = @At("HEAD"), cancellable = true)
    private void swingOffhandInScope(CallbackInfo ci) {
        PlayerLocalMultiplayer self = (PlayerLocalMultiplayer) (Object) this;
        if (OffhandScope.isActive(self)) {
            ((OffhandSwingCapable) self).betterWithHands$swingOffhand();
            NetworkHandler.sendToServer(new OffhandSwingMessage());
            ci.cancel();
        }
    }
}
