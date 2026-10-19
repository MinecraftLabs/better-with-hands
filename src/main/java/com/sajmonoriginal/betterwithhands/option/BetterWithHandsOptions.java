package com.sajmonoriginal.betterwithhands.option;

import com.sajmonoriginal.betterwithhands.util.HandPriority;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.input.InputDevice;
import net.minecraft.client.option.GameSettings;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.option.OptionBoolean;
import net.minecraft.client.option.OptionEnum;
import org.lwjgl.input.Keyboard;

@Environment(EnvType.CLIENT)
public final class BetterWithHandsOptions {
    public static final OptionBoolean USE_OFFHAND_WHEN_MAIN_EMPTY = GameSettings.register(new OptionBoolean("betterWithHands.useOffhandWhenMainEmpty", true));
    public static final OptionBoolean SWAP_HAND_PRIORITY = GameSettings.register(new OptionBoolean("betterWithHands.swapHandPriority", false));
    public static final OptionBoolean DYNAMIC_HAND_PRIORITY = GameSettings.register(new OptionBoolean("betterWithHands.dynamicHandPriority", false));
    public static final OptionEnum<IndicatorType> INDICATOR_TYPE = GameSettings.register(new OptionEnum<>("betterWithHands.indicatorType", IndicatorType.class, IndicatorType.BORDER));
    public static final OptionBoolean SHOW_INDICATOR_ONLY_WITH_OFFHAND = GameSettings.register(new OptionBoolean("betterWithHands.showIndicatorOnlyWithOffhand", true));
    public static final OptionBoolean LEFT_HAND_MODE = GameSettings.register(new OptionBoolean("betterWithHands.leftHandMode", false));
    public static final KeyBinding KEY_SWAP_PRIORITY = GameSettings.register(new KeyBinding("key.betterWithHands.swapPriority").setDefault(InputDevice.keyboard, Keyboard.KEY_X));
    public static final KeyBinding KEY_SWAP_HANDS = GameSettings.register(new KeyBinding("key.betterWithHands.swapHands").setDefault(InputDevice.keyboard, Keyboard.KEY_F));

    private BetterWithHandsOptions() {
    }

    public static HandPriority handPriority() {
        return new HandPriority(SWAP_HAND_PRIORITY.value, USE_OFFHAND_WHEN_MAIN_EMPTY.value, DYNAMIC_HAND_PRIORITY.value);
    }
}
