package com.sajmonoriginal.betterwithhands.gui;

import com.sajmonoriginal.betterwithhands.option.BetterWithHandsOptions;
import com.sajmonoriginal.betterwithhands.option.IndicatorType;
import com.sajmonoriginal.betterwithhands.util.HandLogic;
import com.sajmonoriginal.betterwithhands.util.OffhandCapable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ItemElement;
import net.minecraft.client.gui.hud.HudIngame;
import net.minecraft.client.gui.hud.component.ComponentAnchor;
import net.minecraft.client.gui.hud.component.HudComponent;
import net.minecraft.client.gui.hud.component.HudComponentMovable;
import net.minecraft.client.gui.hud.component.HudComponents;
import net.minecraft.client.gui.hud.component.layout.Layout;
import net.minecraft.client.gui.hud.component.layout.LayoutSnap;
import net.minecraft.client.option.GameSettings;
import net.minecraft.client.render.renderer.BlendFactor;
import net.minecraft.client.render.renderer.GLRenderer;
import net.minecraft.client.render.renderer.State;
import net.minecraft.core.item.ItemStack;

@Environment(EnvType.CLIENT)
public class HudComponentOffhand extends HudComponentMovable {
    private static final String HUD_TEXTURE = "/assets/better_with_hands/textures/gui/hud_offhand.png";
    private static final int SLOT_SIZE = 22;
    private static final int SELECTOR_SIZE = 24;
    private static final int SELECTOR_V = 22;
    private static final int HOTBAR_GAP = 4;
    private static final long EQUIP_ANIMATION_MS = 300L;

    private ItemElement itemElement;

    private ItemStack lastOffhand = null;
    private long animationStartTime = 0L;
    private boolean animatingIn = false;


    public HudComponentOffhand(String key, Layout layout) {
        super(key, SLOT_SIZE, SLOT_SIZE, layout);
    }

    @Override
    public boolean isVisible() {
        return GameSettings.IMMERSIVE_MODE.drawHotbar() && mc.thePlayer != null;
    }

    public static LayoutSnap defaultLayout(boolean leftHanded) {
        return leftHanded
            ? new LayoutSnap(HudComponents.HOTBAR, ComponentAnchor.CENTER_RIGHT, ComponentAnchor.CENTER_LEFT, HOTBAR_GAP, 0)
            : new LayoutSnap(HudComponents.HOTBAR, ComponentAnchor.CENTER_LEFT, ComponentAnchor.CENTER_RIGHT, -HOTBAR_GAP, 0);
    }

    private static boolean isDefaultLayout(Layout layout, boolean leftHanded) {
        LayoutSnap expected = defaultLayout(leftHanded);
        return layout instanceof LayoutSnap snap
            && snap.getParent() == expected.getParent()
            && snap.getParentAnchor() == expected.getParentAnchor()
            && snap.getAnchor() == expected.getAnchor()
            && snap.getXOffset() == expected.getXOffset()
            && snap.getYOffset() == expected.getYOffset();
    }

    private void followHandedness() {
        boolean leftHanded = GameSettings.PLAYER_LEFT_HANDED.value;
        if (isDefaultLayout(getLayout(), !leftHanded)) {
            setLayout(defaultLayout(leftHanded));
        }
    }

    @Override
    public void render(HudIngame hud, int xSizeScreen, int ySizeScreen, float partialTicks) {
        if (mc.thePlayer == null) {
            return;
        }

        followHandedness();

        ItemStack offhand = ((OffhandCapable) mc.thePlayer.inventory).betterWithHands$getOffhandItem();

        boolean itemTypeChanged = !isSameItemType(offhand, this.lastOffhand);
        if (itemTypeChanged) {
            this.animationStartTime = System.currentTimeMillis();
            this.animatingIn = offhand != null;
        }
        this.lastOffhand = offhand != null ? offhand.copy() : null;

        long elapsed = System.currentTimeMillis() - this.animationStartTime;
        float animFraction = Math.min(1.0F, (float) elapsed / EQUIP_ANIMATION_MS);
        float animProgress = this.animatingIn ? animFraction : 1.0F - animFraction;

        int slotX = getLayout().getComponentX(this, xSizeScreen);
        int slotBaseY = getLayout().getComponentY(this, ySizeScreen);

        float slideOffset = (1.0F - animProgress) * 24.0F;
        int slotY = slotBaseY + (int) slideOffset;

        if (animProgress > 0.01F) {
            if (this.itemElement == null) {
                this.itemElement = new ItemElement(mc);
            }

            GLRenderer.pushFrame();
            GLRenderer.setColor4f(1.0F, 1.0F, 1.0F, animProgress);
            GLRenderer.enableState(State.BLEND);
            GLRenderer.setBlendFunc(BlendFactor.SRC_ALPHA, BlendFactor.ONE_MINUS_SRC_ALPHA);

            mc.textureManager.loadTexture(HUD_TEXTURE).bind();
            hud.drawTexturedModalRect(slotX, slotY, 0, 0, SLOT_SIZE, SLOT_SIZE);

            if (offhand != null) {
                this.itemElement.render(offhand, slotX + 3, slotY + 3);
            }

            GLRenderer.popFrame();
        }

        renderPriorityIndicator(hud, slotX, slotBaseY, animProgress, xSizeScreen, ySizeScreen);
    }

    @Override
    public void renderPreview(Gui gui, Layout layout, int xSizeScreen, int ySizeScreen) {
        followHandedness();

        int x = layout.getComponentX(this, xSizeScreen);
        int y = layout.getComponentY(this, ySizeScreen);

        GLRenderer.pushFrame();
        GLRenderer.setColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GLRenderer.enableState(State.BLEND);
        GLRenderer.setBlendFunc(BlendFactor.SRC_ALPHA, BlendFactor.ONE_MINUS_SRC_ALPHA);

        mc.textureManager.loadTexture(HUD_TEXTURE).bind();
        gui.drawTexturedModalRect(x, y, 0, 0, SLOT_SIZE, SLOT_SIZE);
        gui.drawTexturedModalRect(x - 1, y - 1, 0, SELECTOR_V, SELECTOR_SIZE, SELECTOR_SIZE);

        GLRenderer.popFrame();
    }

    private void renderPriorityIndicator(HudIngame hud, int offhandSlotX, int offhandSlotY,
                                         float offhandAnimProgress, int xSizeScreen, int ySizeScreen) {
        IndicatorType indicatorType = BetterWithHandsOptions.INDICATOR_TYPE.value;
        if (indicatorType == IndicatorType.NONE) {
            return;
        }

        boolean swapPriority = BetterWithHandsOptions.SWAP_HAND_PRIORITY.value;
        boolean useWhenEmpty = BetterWithHandsOptions.USE_OFFHAND_WHEN_MAIN_EMPTY.value;
        boolean showOnlyWithOffhand = BetterWithHandsOptions.SHOW_INDICATOR_ONLY_WITH_OFFHAND.value;

        ItemStack offhand = ((OffhandCapable) mc.thePlayer.inventory).betterWithHands$getOffhandItem();
        ItemStack mainhand = mc.thePlayer.inventory.getCurrentItem();

        boolean dualWieldSwords = HandLogic.isDualWieldingSwords(mainhand, offhand);

        boolean offhandActive = false;
        if (offhand != null) {
            if (swapPriority) {
                offhandActive = true;
            } else if (useWhenEmpty && mainhand == null) {
                offhandActive = true;
            }
        }

        boolean offhandSlotVisible = offhandAnimProgress > 0.01F;

        if (showOnlyWithOffhand && offhand == null && !offhandSlotVisible) {
            return;
        }

        int mainSlotX;
        int mainSlotY;

        HudComponent hotbar = HudComponents.HOTBAR;
        int hotbarX = hotbar.getLayout().getComponentX(hotbar, xSizeScreen);
        int hotbarY = hotbar.getLayout().getComponentY(hotbar, ySizeScreen);
        int selectedSlot = mc.thePlayer.inventory.getCurrentSlot();
        int hotbarOffset = mc.thePlayer.inventory.getHotbarOffset();
        int relativeSlot = selectedSlot - hotbarOffset;
        mainSlotX = hotbarX + 4 + relativeSlot * 20;
        mainSlotY = hotbarY - 1;

        int offIndicatorX = offhandSlotX - 1;
        int offIndicatorY = offhandSlotY - 1;

        GLRenderer.pushFrame();
        GLRenderer.enableState(State.BLEND);
        GLRenderer.setBlendFunc(BlendFactor.SRC_ALPHA, BlendFactor.ONE_MINUS_SRC_ALPHA);
        GLRenderer.setColor4f(1.0F, 1.0F, 1.0F, 1.0F);

        if (indicatorType == IndicatorType.BORDER) {
            mc.textureManager.loadTexture("/assets/better_with_hands/textures/gui/selector.png").bind();

            if (dualWieldSwords && offhandSlotVisible) {
                hud.drawTexturedModalRect(offIndicatorX, offIndicatorY, 0, 0, 24, 24, 1.0 / 24, 1.0 / 24);
                hud.drawTexturedModalRect(mainSlotX, mainSlotY, 0, 0, 24, 24, 1.0 / 24, 1.0 / 24);
            } else if (offhandActive && offhandSlotVisible) {
                hud.drawTexturedModalRect(offIndicatorX, offIndicatorY, 0, 0, 24, 24, 1.0 / 24, 1.0 / 24);
            } else if (!offhandActive) {
                hud.drawTexturedModalRect(mainSlotX, mainSlotY, 0, 0, 24, 24, 1.0 / 24, 1.0 / 24);
            }
        } else if (indicatorType == IndicatorType.DOT) {
            mc.textureManager.loadTexture("/assets/better_with_hands/textures/gui/dot.png").bind();

            int dotW = 5, dotH = 3;
            int mainDotX = mainSlotX + (24 - dotW) / 2;
            int mainDotY = mainSlotY;
            int offDotX = offIndicatorX + (24 - dotW) / 2;
            int offDotY = offIndicatorY;

            if (dualWieldSwords && offhandSlotVisible) {
                hud.drawTexturedModalRect(offDotX, offDotY, 0, 0, dotW, dotH, 1.0 / dotW, 1.0 / dotH);
                hud.drawTexturedModalRect(mainDotX, mainDotY, 0, 0, dotW, dotH, 1.0 / dotW, 1.0 / dotH);
            } else if (offhandActive && offhandSlotVisible) {
                hud.drawTexturedModalRect(offDotX, offDotY, 0, 0, dotW, dotH, 1.0 / dotW, 1.0 / dotH);
            } else if (!offhandActive) {
                hud.drawTexturedModalRect(mainDotX, mainDotY, 0, 0, dotW, dotH, 1.0 / dotW, 1.0 / dotH);
            }
        } else if (indicatorType == IndicatorType.ARROW) {
            mc.textureManager.loadTexture("/assets/better_with_hands/textures/gui/priority_indicator.png").bind();

            int arrowW = 5, arrowH = 5;
            int mainArrowX = mainSlotX + (24 - arrowW) / 2;
            int mainArrowY = mainSlotY;
            int offArrowX = offIndicatorX + (24 - arrowW) / 2;
            int offArrowY = offIndicatorY;

            if (dualWieldSwords && offhandSlotVisible) {
                hud.drawTexturedModalRect(offArrowX, offArrowY, 0, 0, arrowW, arrowH, 1.0 / arrowW, 1.0 / arrowH);
                hud.drawTexturedModalRect(mainArrowX, mainArrowY, 0, 0, arrowW, arrowH, 1.0 / arrowW, 1.0 / arrowH);
            } else if (offhandActive && offhandSlotVisible) {
                hud.drawTexturedModalRect(offArrowX, offArrowY, 0, 0, arrowW, arrowH, 1.0 / arrowW, 1.0 / arrowH);
            } else if (!offhandActive) {
                hud.drawTexturedModalRect(mainArrowX, mainArrowY, 0, 0, arrowW, arrowH, 1.0 / arrowW, 1.0 / arrowH);
            }
        }

        GLRenderer.popFrame();
    }

    private boolean isSameItemType(ItemStack a, ItemStack b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return a.itemID == b.itemID && a.getMetadata() == b.getMetadata();
    }
}
