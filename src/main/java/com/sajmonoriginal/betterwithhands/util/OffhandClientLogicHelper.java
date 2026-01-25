package com.sajmonoriginal.betterwithhands.util;

import com.sajmonoriginal.betterwithhands.option.BetterWithHandsOptions;
import net.minecraft.client.Minecraft;
import net.minecraft.core.block.Block;
import net.minecraft.core.item.ItemStack;

public class OffhandClientLogicHelper {
    public static boolean shouldUseOffhandClient(ItemStack mainHand) {
        return shouldUseOffhandClientForBlock(mainHand, null, null);
    }

    public static boolean shouldUseOffhandClientForBlock(ItemStack mainHand, ItemStack offHand, Block<?> block) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.gameSettings == null) {
            return mainHand == null;
        }

        BetterWithHandsOptions options = (BetterWithHandsOptions) mc.gameSettings;
        boolean dynamicPriority = options.betterWithHands$getDynamicHandPriority().value;
        
        if (dynamicPriority && block != null && offHand != null) {
            return DynamicPriorityHelper.shouldUseOffhandForAction(
                mainHand, offHand, DynamicPriorityHelper.ActionType.MINING, block);
        }

        boolean swapPriority = options.betterWithHands$getSwapHandPriority().value;
        boolean useWhenEmpty = options.betterWithHands$getUseOffhandWhenMainEmpty().value;

        if (swapPriority) {
            return true;
        } else if (useWhenEmpty && mainHand == null) {
            return true;
        }
        return false;
    }
}
