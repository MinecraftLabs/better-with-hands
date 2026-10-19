package com.sajmonoriginal.betterwithhands.util;

import net.minecraft.core.block.Block;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.item.block.ItemBlock;
import net.minecraft.core.item.tool.ItemTool;
import net.minecraft.core.item.tool.ItemToolSword;
import org.jetbrains.annotations.Nullable;

public final class DynamicPriorityHelper {
    private DynamicPriorityHelper() {
    }

    public static boolean shouldUseOffhand(@Nullable ItemStack mainHand, ItemStack offhand, HandAction action, @Nullable Block<?> block) {
        if (mainHand == null) {
            return true;
        }
        return switch (action) {
            case MINE -> block != null && offhand.getStrVsBlock(block) > mainHand.getStrVsBlock(block);
            case ATTACK, SWING -> shouldUseOffhandForAttack(mainHand, offhand);
            case USE -> offhand.getItem() instanceof ItemBlock && !(mainHand.getItem() instanceof ItemBlock);
            case DROP -> false;
        };
    }

    private static boolean shouldUseOffhandForAttack(ItemStack mainHand, ItemStack offhand) {
        boolean mainIsSword = mainHand.getItem() instanceof ItemToolSword;
        if (offhand.getItem() instanceof ItemToolSword) {
            return !mainIsSword;
        }
        return offhand.getItem() instanceof ItemTool && !mainIsSword && !(mainHand.getItem() instanceof ItemTool);
    }
}
