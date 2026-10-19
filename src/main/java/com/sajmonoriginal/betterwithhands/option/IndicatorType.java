package com.sajmonoriginal.betterwithhands.option;

import net.minecraft.core.util.helper.ITranslatable;

public enum IndicatorType implements ITranslatable {
    BORDER,
    VANILLA,
    DOT,
    ARROW,
    NONE;

    @Override
    public String getTranslationKey() {
        return this.name().toLowerCase();
    }
}
