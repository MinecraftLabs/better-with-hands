package com.sajmonoriginal.betterwithhands.mixin.client.hud;

import com.sajmonoriginal.betterwithhands.BetterWithHandsClientMod;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.hud.component.HudComponents;
import org.spongepowered.asm.mixin.Mixin;

@Environment(EnvType.CLIENT)
@Mixin(value = HudComponents.class, remap = false)
public abstract class HudComponentEntrypointMixin {
    static {
        BetterWithHandsClientMod.registerHudComponents();
    }
}
