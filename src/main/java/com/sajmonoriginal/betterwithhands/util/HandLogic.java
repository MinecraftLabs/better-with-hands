package com.sajmonoriginal.betterwithhands.util;

import net.minecraft.core.block.Block;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.item.tool.ItemToolSword;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public final class HandLogic {
    private HandLogic() {
    }

    public static boolean isDualWieldingSwords(@Nullable ItemStack mainHand, @Nullable ItemStack offhand) {
        return mainHand != null && offhand != null
            && mainHand.getItem() instanceof ItemToolSword
            && offhand.getItem() instanceof ItemToolSword;
    }

    public static boolean prefersOffhand(Player player, HandAction action, @Nullable Block<?> block) {
        OffhandCapable hands = (OffhandCapable) player.inventory;
        ItemStack offhand = hands.betterWithHands$getOffhandItem();
        if (offhand == null || !hands.betterWithHands$isOffhandEnabled() || hands.betterWithHands$isOffhandActionActive()
            || player.getHeldObject() != null) {
            return false;
        }

        ItemStack mainHand = player.inventory.getCurrentItem();
        if (isDualWieldingSwords(mainHand, offhand)) {
            return (action == HandAction.MINE || action == HandAction.SWING) && !player.isLeftHanded();
        }

        HandPriority priority = hands.betterWithHands$getHandPriority();
        if (priority.dynamic() && action != HandAction.DROP) {
            return DynamicPriorityHelper.shouldUseOffhand(mainHand, offhand, action, block);
        }
        return priority.swapPriority() || priority.useWhenEmpty() && mainHand == null;
    }

    public static boolean isOffhandClick(Player player, int clickType) {
        return (clickType == 0) != player.isLeftHanded();
    }

    public static boolean isOffhandPrimary(Player player) {
        OffhandCapable hands = (OffhandCapable) player.inventory;
        if (hands.betterWithHands$getOffhandItem() == null) {
            return false;
        }
        HandPriority priority = hands.betterWithHands$getHandPriority();
        return priority.swapPriority() || priority.useWhenEmpty() && player.inventory.getCurrentItem() == null;
    }

    public static <T> T perform(Player player, HandAction action, @Nullable Block<?> block, Supplier<T> task) {
        return prefersOffhand(player, action, block) ? OffhandScope.call(player, task) : task.get();
    }

    public static void perform(Player player, HandAction action, @Nullable Block<?> block, Runnable task) {
        if (prefersOffhand(player, action, block)) {
            OffhandScope.run(player, task);
        } else {
            task.run();
        }
    }
}
