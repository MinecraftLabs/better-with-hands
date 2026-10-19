package com.sajmonoriginal.betterwithhands.util;

import net.minecraft.core.entity.player.Player;

import java.util.function.Supplier;

public final class OffhandScope {
    private OffhandScope() {
    }

    public static boolean isActive(Player player) {
        return ((OffhandCapable) player.inventory).betterWithHands$isOffhandActionActive();
    }

    public static <T> T call(Player player, Supplier<T> action) {
        OffhandCapable hands = (OffhandCapable) player.inventory;
        if (!hands.betterWithHands$beginOffhandAction()) {
            return action.get();
        }
        try {
            return action.get();
        } finally {
            hands.betterWithHands$endOffhandAction();
        }
    }

    public static void run(Player player, Runnable action) {
        call(player, () -> {
            action.run();
            return null;
        });
    }
}
