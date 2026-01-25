package com.sajmonoriginal.betterwithhands.util;

public class DualWieldHelper {
    private static boolean useOffhandForAttack = false;

    public static void setUseOffhandForAttack(boolean value) {
        useOffhandForAttack = value;
    }

    public static boolean shouldUseOffhandForAttack() {
        return useOffhandForAttack;
    }

    public static void clearUseOffhandForAttack() {
        useOffhandForAttack = false;
    }
}
