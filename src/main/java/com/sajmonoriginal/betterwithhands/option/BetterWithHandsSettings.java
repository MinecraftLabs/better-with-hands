package com.sajmonoriginal.betterwithhands.option;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.options.components.KeyBindingComponent;
import net.minecraft.client.gui.options.components.OptionsCategory;
import net.minecraft.client.gui.options.components.ToggleableOptionComponent;
import net.minecraft.client.gui.options.data.OptionsPage;
import net.minecraft.client.gui.options.data.OptionsPages;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.item.Items;

@Environment(EnvType.CLIENT)
public class BetterWithHandsSettings {

    private BetterWithHandsSettings() {}

    private static boolean initialized = false;

    public static void init() {
        if (!initialized) {
            initialized = true;
            registerSettings();
        }
    }

    public static void registerSettings() {
        OptionsPage page = new OptionsPage(
            "gui.options.page.better_with_hands.title",
            new ItemStack(Items.LEATHER)
        ).withComponent(
            new OptionsCategory("gui.options.page.better_with_hands.category.behavior")
                .withComponent(new ToggleableOptionComponent<>(BetterWithHandsOptions.USE_OFFHAND_WHEN_MAIN_EMPTY))
                .withComponent(new ToggleableOptionComponent<>(BetterWithHandsOptions.SWAP_HAND_PRIORITY))
                .withComponent(new ToggleableOptionComponent<>(BetterWithHandsOptions.DYNAMIC_HAND_PRIORITY))
        ).withComponent(
            new OptionsCategory("gui.options.page.better_with_hands.category.display")
                .withComponent(new ToggleableOptionComponent<>(BetterWithHandsOptions.INDICATOR_TYPE))
                .withComponent(new ToggleableOptionComponent<>(BetterWithHandsOptions.SHOW_INDICATOR_ONLY_WITH_OFFHAND))
        ).withComponent(
            new OptionsCategory("gui.options.page.better_with_hands.category.controls")
                .withComponent(new KeyBindingComponent(BetterWithHandsOptions.KEY_SWAP_HANDS))
                .withComponent(new KeyBindingComponent(BetterWithHandsOptions.KEY_SWAP_PRIORITY))
        );

        OptionsPages.register(page);
    }
}
