package com.sajmonoriginal.betterwithhands.option;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
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
        BetterWithHandsOptions options = (BetterWithHandsOptions) Minecraft.getMinecraft().gameSettings;

        OptionsPage page = new OptionsPage(
            "gui.options.page.betterwithhands.title",
            new ItemStack(Items.LEATHER)
        ).withComponent(
            new OptionsCategory("gui.options.page.betterwithhands.category.behavior")
                .withComponent(new ToggleableOptionComponent<>(options.betterWithHands$getUseOffhandWhenMainEmpty()))
                .withComponent(new ToggleableOptionComponent<>(options.betterWithHands$getSwapHandPriority()))
                .withComponent(new ToggleableOptionComponent<>(options.betterWithHands$getDynamicHandPriority()))
        ).withComponent(
            new OptionsCategory("gui.options.page.betterwithhands.category.display")
                .withComponent(new ToggleableOptionComponent<>(options.betterWithHands$getLeftHandMode()))
                .withComponent(new ToggleableOptionComponent<>(options.betterWithHands$getIndicatorType()))
                .withComponent(new ToggleableOptionComponent<>(options.betterWithHands$getShowIndicatorOnlyWithOffhand()))
        ).withComponent(
            new OptionsCategory("gui.options.page.betterwithhands.category.controls")
                .withComponent(new KeyBindingComponent(options.betterWithHands$getKeySwapHands()))
                .withComponent(new KeyBindingComponent(options.betterWithHands$getKeySwapPriority()))
        );
        
        OptionsPages.register(page);
    }
}
