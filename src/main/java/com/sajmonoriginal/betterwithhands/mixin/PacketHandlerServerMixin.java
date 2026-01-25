package com.sajmonoriginal.betterwithhands.mixin;

import org.spongepowered.asm.mixin.Mixin;
import net.minecraft.server.net.handler.PacketHandlerServer;

@Mixin(value = PacketHandlerServer.class, remap = false)
public class PacketHandlerServerMixin {
}
