package com.sajmonoriginal.betterwithhands.util;

public record HandPriority(boolean swapPriority, boolean useWhenEmpty, boolean dynamic) {
    public static final HandPriority DEFAULT = new HandPriority(false, true, false);
    public static final HandPriority DISABLED = new HandPriority(false, false, false);
}
