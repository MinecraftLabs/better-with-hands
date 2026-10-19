package com.sajmonoriginal.betterwithhands.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.sajmonoriginal.betterwithhands.client.HandIndicator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.hud.HudIngame;
import net.minecraft.client.gui.hud.component.HudComponentHotbar;
import net.minecraft.client.render.tessellator.TessellatorGeneral;
import net.minecraft.client.render.texture.stitcher.IconCoordinate;
import net.minecraft.core.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Environment(EnvType.CLIENT)
@Mixin(value = HudComponentHotbar.class, remap = false)
public abstract class HudComponentHotbarMixin {
    @WrapOperation(
        method = "drawOrientedBackgrounds",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;addGuiIconDouble(Lnet/minecraft/client/render/tessellator/TessellatorGeneral;DDDDLnet/minecraft/client/render/texture/stitcher/IconCoordinate;)V")
    )
    private void addMainHandIndicator(Gui gui, TessellatorGeneral tessellator, double x, double y, double width, double height,
                                      IconCoordinate icon, Operation<Void> original) {
        original.call(gui, tessellator, x, y, width, height, icon);
        Player player = Minecraft.getMinecraft().thePlayer;
        boolean isSelection = width == HandIndicator.SELECTION_SIZE && height == HandIndicator.SELECTION_SIZE;
        if (isSelection && gui instanceof HudIngame && player != null && HandIndicator.highlightMainHand(player)) {
            HandIndicator.addIndicator(gui, tessellator, x, y);
        }
    }
}
