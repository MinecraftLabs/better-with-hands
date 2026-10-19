package com.sajmonoriginal.betterwithhands.mixin.client;

import com.sajmonoriginal.betterwithhands.client.ClientHands;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.entity.player.PlayerLocal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(value = PlayerLocal.class, remap = false)
public abstract class PlayerLocalMixin {
    @Inject(method = "onLivingUpdate", at = @At("HEAD"))
    private void syncHandPriority(CallbackInfo ci) {
        ClientHands.syncHandPriority((PlayerLocal) (Object) this);
    }
}
