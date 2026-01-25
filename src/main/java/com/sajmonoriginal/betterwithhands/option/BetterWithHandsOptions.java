package com.sajmonoriginal.betterwithhands.option;

import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.option.OptionBoolean;
import net.minecraft.client.option.OptionEnum;

public interface BetterWithHandsOptions {
    OptionBoolean betterWithHands$getUseOffhandWhenMainEmpty();
    OptionBoolean betterWithHands$getSwapHandPriority();
    OptionBoolean betterWithHands$getDynamicHandPriority();
    OptionEnum<IndicatorType> betterWithHands$getIndicatorType();
    OptionBoolean betterWithHands$getShowIndicatorOnlyWithOffhand();
    OptionBoolean betterWithHands$getLeftHandMode();
    KeyBinding betterWithHands$getKeySwapPriority();
    KeyBinding betterWithHands$getKeySwapHands();
}
