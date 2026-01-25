package com.sajmonoriginal.betterwithhands.util;

import net.minecraft.core.block.Block;
import net.minecraft.core.item.Item;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.item.block.ItemBlock;
import net.minecraft.core.item.tool.ItemTool;
import net.minecraft.core.item.tool.ItemToolSword;

public class DynamicPriorityHelper {

    public enum ActionType {
        MINING,
        ATTACK,
        USE
    }

    public static boolean isDualWieldingSwords(ItemStack mainHand, ItemStack offHand) {
        if (mainHand == null || offHand == null) {
            return false;
        }
        return mainHand.getItem() instanceof ItemToolSword && offHand.getItem() instanceof ItemToolSword;
    }

    public static boolean shouldUseOffhandForAction(ItemStack mainHand, ItemStack offHand, ActionType action, Object context) {
        if (offHand == null) {
            return false;
        }
        if (mainHand == null) {
            return true;
        }

        switch (action) {
            case MINING:
                if (context instanceof Block) {
                    return shouldUseOffhandForMining(mainHand, offHand, (Block<?>) context);
                }
                break;
            case ATTACK:
                return shouldUseOffhandForAttack(mainHand, offHand);
            case USE:
                return shouldUseOffhandForUse(mainHand, offHand);
        }
        return false;
    }

    private static boolean shouldUseOffhandForMining(ItemStack mainHand, ItemStack offHand, Block<?> block) {
        float mainSpeed = getToolEfficiency(mainHand, block);
        float offSpeed = getToolEfficiency(offHand, block);

        return offSpeed > mainSpeed;
    }

    private static float getToolEfficiency(ItemStack stack, Block<?> block) {
        if (stack == null || stack.getItem() == null) {
            return 1.0F;
        }
        return stack.getStrVsBlock(block);
    }

    private static boolean shouldUseOffhandForAttack(ItemStack mainHand, ItemStack offHand) {
        boolean mainIsSword = mainHand.getItem() instanceof ItemToolSword;
        boolean offIsSword = offHand.getItem() instanceof ItemToolSword;

        if (offIsSword && !mainIsSword) {
            return true;
        }

        boolean mainIsTool = mainHand.getItem() instanceof ItemTool;
        boolean offIsTool = offHand.getItem() instanceof ItemTool;

        if (offIsSword && mainIsSword) {
            return false;
        }
        if (offIsTool && !mainIsTool && !mainIsSword) {
            return true;
        }

        return false;
    }

    private static boolean shouldUseOffhandForUse(ItemStack mainHand, ItemStack offHand) {
        boolean mainIsBlock = isPlaceableBlock(mainHand);
        boolean offIsBlock = isPlaceableBlock(offHand);

        if (offIsBlock && !mainIsBlock) {
            return true;
        }

        return false;
    }

    private static boolean isPlaceableBlock(ItemStack stack) {
        if (stack == null || stack.getItem() == null) {
            return false;
        }
        return stack.getItem() instanceof ItemBlock;
    }
}
