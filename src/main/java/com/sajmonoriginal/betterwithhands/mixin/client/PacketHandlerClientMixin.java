package com.sajmonoriginal.betterwithhands.mixin.client;

import com.sajmonoriginal.betterwithhands.client.ClientHands;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.net.handler.PacketHandlerClient;
import net.minecraft.core.net.packet.PacketLogin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(value = PacketHandlerClient.class, remap = false)
public abstract class PacketHandlerClientMixin {
    @Inject(method = "handleLogin", at = @At("HEAD"))
    private void resetServerSupport(PacketLogin packet, CallbackInfo ci) {
        ClientHands.onLogin();
    }
}
