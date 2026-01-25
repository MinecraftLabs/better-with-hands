package com.sajmonoriginal.betterwithhands.util;

import net.minecraft.core.block.Block;
import net.minecraft.core.item.ItemStack;

public class OffhandLogicHelper {
    public static boolean shouldUseOffhand(ItemStack mainHand, ItemStack offhandItem, OffhandCapable inventory) {
        return shouldUseOffhandForBlock(mainHand, offhandItem, inventory, null);
    }

    public static boolean shouldUseOffhandForBlock(ItemStack mainHand, ItemStack offhandItem, OffhandCapable inventory, Block<?> block) {
        if (offhandItem == null) {
            return false;
        }

        if (net.fabricmc.loader.api.FabricLoader.getInstance().getEnvironmentType() == net.fabricmc.api.EnvType.CLIENT) {
            return OffhandClientLogicHelper.shouldUseOffhandClientForBlock(mainHand, offhandItem, block);
        }

        boolean dynamicPriority = inventory.betterWithHands$getDynamicPriority();
        if (dynamicPriority && block != null) {
            return DynamicPriorityHelper.shouldUseOffhandForAction(
                mainHand, offhandItem, DynamicPriorityHelper.ActionType.MINING, block);
        }

        boolean swapPriority = inventory.betterWithHands$getSwapPriority();
        boolean useWhenEmpty = inventory.betterWithHands$getUseWhenEmpty();

        if (swapPriority) {
            return true;
        } else if (useWhenEmpty && mainHand == null) {
            return true;
        }
        return false;
    }
}
