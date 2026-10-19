package com.sajmonoriginal.betterwithhands.util;

import net.minecraft.core.item.ItemStack;

public interface OffhandCapable {
    ItemStack betterWithHands$getOffhandItem();

    void betterWithHands$setOffhandItem(ItemStack stack);

    void betterWithHands$swapHands();

    boolean betterWithHands$beginOffhandAction();

    void betterWithHands$endOffhandAction();

    boolean betterWithHands$isOffhandActionActive();

    boolean betterWithHands$isOffhandEnabled();

    void betterWithHands$setOffhandEnabled(boolean enabled);

    HandPriority betterWithHands$getHandPriority();

    void betterWithHands$setHandPriority(HandPriority priority);
}
