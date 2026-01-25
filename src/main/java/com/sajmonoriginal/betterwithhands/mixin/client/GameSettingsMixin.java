package com.sajmonoriginal.betterwithhands.mixin.client;

import com.sajmonoriginal.betterwithhands.option.BetterWithHandsOptions;
import com.sajmonoriginal.betterwithhands.option.IndicatorType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.input.InputDevice;
import net.minecraft.client.option.GameSettings;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.option.OptionBoolean;
import net.minecraft.client.option.OptionEnum;
import org.lwjgl.input.Keyboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Environment(EnvType.CLIENT)
@Mixin(value = GameSettings.class, remap = false)
public class GameSettingsMixin implements BetterWithHandsOptions {
    
    @Unique
    private OptionBoolean betterWithHands$useOffhandWhenMainEmpty;
    
    @Unique
    private OptionBoolean betterWithHands$swapHandPriority;

    @Unique
    private OptionEnum<IndicatorType> betterWithHands$indicatorType;

    @Unique
    private OptionBoolean betterWithHands$showIndicatorOnlyWithOffhand;

    @Unique
    private OptionBoolean betterWithHands$leftHandMode;

    @Unique
    private OptionBoolean betterWithHands$dynamicHandPriority;

    @Unique
    private KeyBinding betterWithHands$keySwapPriority;

    @Unique
    private KeyBinding betterWithHands$keySwapHands;

    @Override
    public OptionBoolean betterWithHands$getUseOffhandWhenMainEmpty() {
        if (betterWithHands$useOffhandWhenMainEmpty == null) {
            betterWithHands$useOffhandWhenMainEmpty = new OptionBoolean((GameSettings) (Object) this, "betterWithHands.useOffhandWhenMainEmpty", true);
        }
        return betterWithHands$useOffhandWhenMainEmpty;
    }

    @Override
    public OptionBoolean betterWithHands$getSwapHandPriority() {
        if (betterWithHands$swapHandPriority == null) {
            betterWithHands$swapHandPriority = new OptionBoolean((GameSettings) (Object) this, "betterWithHands.swapHandPriority", false);
        }
        return betterWithHands$swapHandPriority;
    }

    @Override
    public OptionEnum<IndicatorType> betterWithHands$getIndicatorType() {
        if (betterWithHands$indicatorType == null) {
            betterWithHands$indicatorType = new OptionEnum<>((GameSettings) (Object) this, "betterWithHands.indicatorType", IndicatorType.class, IndicatorType.BORDER);
        }
        return betterWithHands$indicatorType;
    }

    @Override
    public OptionBoolean betterWithHands$getShowIndicatorOnlyWithOffhand() {
        if (betterWithHands$showIndicatorOnlyWithOffhand == null) {
            betterWithHands$showIndicatorOnlyWithOffhand = new OptionBoolean((GameSettings) (Object) this, "betterWithHands.showIndicatorOnlyWithOffhand", true);
        }
        return betterWithHands$showIndicatorOnlyWithOffhand;
    }

    @Override
    public OptionBoolean betterWithHands$getLeftHandMode() {
        if (betterWithHands$leftHandMode == null) {
            betterWithHands$leftHandMode = new OptionBoolean((GameSettings) (Object) this, "betterWithHands.leftHandMode", false);
        }
        return betterWithHands$leftHandMode;
    }

    @Override
    public OptionBoolean betterWithHands$getDynamicHandPriority() {
        if (betterWithHands$dynamicHandPriority == null) {
            betterWithHands$dynamicHandPriority = new OptionBoolean((GameSettings) (Object) this, "betterWithHands.dynamicHandPriority", false);
        }
        return betterWithHands$dynamicHandPriority;
    }

    @Override
    public KeyBinding betterWithHands$getKeySwapPriority() {
        if (betterWithHands$keySwapPriority == null) {
            betterWithHands$keySwapPriority = new KeyBinding("key.betterWithHands.swapPriority").setDefault(InputDevice.keyboard, Keyboard.KEY_X);
        }
        return betterWithHands$keySwapPriority;
    }

    @Override
    public KeyBinding betterWithHands$getKeySwapHands() {
        if (betterWithHands$keySwapHands == null) {
            betterWithHands$keySwapHands = new KeyBinding("key.betterWithHands.swapHands").setDefault(InputDevice.keyboard, Keyboard.KEY_F);
        }
        return betterWithHands$keySwapHands;
    }
}
