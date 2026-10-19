package com.sajmonoriginal.betterwithhands.mixin.server;

import com.sajmonoriginal.betterwithhands.network.ServerHandshakeMessage;
import net.minecraft.server.entity.player.PlayerServer;
import net.minecraft.server.net.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import turniplabs.halplibe.helper.network.NetworkHandler;

@Mixin(value = PlayerList.class, remap = false)
public abstract class PlayerListMixin {
    @Inject(method = "playerLoggedIn", at = @At("TAIL"))
    private void announceOffhandSupport(PlayerServer player, CallbackInfo ci) {
        NetworkHandler.sendToPlayer(player, new ServerHandshakeMessage());
    }
}
