package com.sajmonoriginal.betterwithhands.util;

import net.minecraft.core.item.ItemStack;

public interface OffhandCapable {
    ItemStack betterWithHands$getOffhandItem();
    void betterWithHands$setOffhandItem(ItemStack stack);
    void betterWithHands$swapHands();
    boolean betterWithHands$isUsingOffhand();
    void betterWithHands$setUsingOffhand(boolean using);
    boolean betterWithHands$getSwapPriority();
    void betterWithHands$setSwapPriority(boolean swapPriority);
    boolean betterWithHands$getUseWhenEmpty();
    void betterWithHands$setUseWhenEmpty(boolean useWhenEmpty);
    boolean betterWithHands$getDynamicPriority();
    void betterWithHands$setDynamicPriority(boolean dynamicPriority);
}
