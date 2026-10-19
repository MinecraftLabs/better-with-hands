package com.sajmonoriginal.betterwithhands.client;

import com.sajmonoriginal.betterwithhands.option.BetterWithHandsOptions;
import com.sajmonoriginal.betterwithhands.option.IndicatorType;
import com.sajmonoriginal.betterwithhands.util.HandLogic;
import com.sajmonoriginal.betterwithhands.util.OffhandCapable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.render.tessellator.TessellatorGeneral;
import net.minecraft.client.render.texture.stitcher.TextureRegistry;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.item.ItemStack;

@Environment(EnvType.CLIENT)
public final class HandIndicator {
    public static final int SELECTION_SIZE = 24;
    private static final int MARKER_WIDTH = 5;

    private HandIndicator() {
    }

    public static IndicatorType type() {
        return BetterWithHandsOptions.INDICATOR_TYPE.value;
    }

    public static boolean highlightOffhand(Player player) {
        ItemStack offhand = ((OffhandCapable) player.inventory).betterWithHands$getOffhandItem();
        if (type() == IndicatorType.NONE || offhand == null) {
            return false;
        }
        return HandLogic.isDualWieldingSwords(player.inventory.getCurrentItem(), offhand) || HandLogic.isOffhandPrimary(player);
    }

    public static boolean highlightMainHand(Player player) {
        if (type() == IndicatorType.NONE || !((OffhandCapable) player.inventory).betterWithHands$isOffhandEnabled()) {
            return false;
        }
        ItemStack offhand = ((OffhandCapable) player.inventory).betterWithHands$getOffhandItem();
        if (offhand == null) {
            return !BetterWithHandsOptions.SHOW_INDICATOR_ONLY_WITH_OFFHAND.value;
        }
        return HandLogic.isDualWieldingSwords(player.inventory.getCurrentItem(), offhand) || !HandLogic.isOffhandPrimary(player);
    }

    public static void addIndicator(Gui gui, TessellatorGeneral tessellator, double x, double y) {
        double markerX = x + (SELECTION_SIZE - MARKER_WIDTH) / 2;
        switch (type()) {
            case BORDER -> gui.addGuiIconDouble(tessellator, x, y, SELECTION_SIZE, SELECTION_SIZE,
                TextureRegistry.getTexture("better_with_hands:gui/hud/hand_border"));
            case VANILLA -> gui.addGuiIconDouble(tessellator, x, y, SELECTION_SIZE, SELECTION_SIZE,
                TextureRegistry.getTexture("better_with_hands:gui/hud/hand_vanilla"));
            case DOT -> gui.addGuiIconDouble(tessellator, markerX, y, MARKER_WIDTH, 3,
                TextureRegistry.getTexture("better_with_hands:gui/hud/hand_dot"));
            case ARROW -> gui.addGuiIconDouble(tessellator, markerX, y, MARKER_WIDTH, 5,
                TextureRegistry.getTexture("better_with_hands:gui/hud/hand_arrow"));
            case NONE -> {
            }
        }
    }
}
