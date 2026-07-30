package com.sajmonoriginal.betterwithhands.mixin.client;

import com.sajmonoriginal.betterwithhands.option.BetterWithHandsOptions;
import com.sajmonoriginal.betterwithhands.util.OffhandCapable;
import com.sajmonoriginal.betterwithhands.util.OffhandSwingCapable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.render.ItemRenderer;
import net.minecraft.client.render.renderer.CullFace;
import net.minecraft.client.render.renderer.GLRenderer;
import net.minecraft.core.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(value = ItemRenderer.class, remap = false)
public abstract class ItemRendererMixin {
    @Shadow
    private Minecraft mc;

    @Shadow
    private ItemStack itemToRender;

    @Shadow
    private float height;

    @Shadow
    private float oHeight;

    @Shadow
    public abstract void renderItemInFirstPerson(float partialTicks);

    @Unique
    private boolean betterWithHands$renderingSecondHand = false;

    @Unique
    private int betterWithHands$lastOffhandItemId = -1;
    @Unique
    private int betterWithHands$lastOffhandItemMeta = -1;
    @Unique
    private long betterWithHands$animationStartTime = 0L;
    @Unique
    private static final long EQUIP_ANIMATION_MS = 300L;
    @Unique
    private ItemStack betterWithHands$lastRenderedOffhand = null;
    @Unique
    private boolean betterWithHands$animatingIn = false;

    @Inject(method = "renderItemInFirstPerson", at = @At("HEAD"), cancellable = true)
    private void renderWithLeftHandMode(float partialTicks, CallbackInfo ci) {
        if (this.betterWithHands$renderingSecondHand) {
            return;
        }

        if (this.mc.thePlayer == null) {
            return;
        }

        boolean leftHandMode = BetterWithHandsOptions.LEFT_HAND_MODE.value;

        if (leftHandMode) {
            ci.cancel();

            GLRenderer.pushFrame();

            GLRenderer.setCullFace(CullFace.FRONT);
            GLRenderer.modelM4f().scale(-1.0F, 1.0F, 1.0F);

            this.betterWithHands$renderingSecondHand = true;
            this.renderItemInFirstPerson(partialTicks);
            this.betterWithHands$renderingSecondHand = false;

            GLRenderer.popFrame();

            betterWithHands$renderOffhand(partialTicks, true);
        }
    }

    @Inject(method = "renderItemInFirstPerson", at = @At("RETURN"))
    private void renderOffhandItemAfter(float partialTicks, CallbackInfo ci) {
        if (this.betterWithHands$renderingSecondHand) {
            return;
        }

        if (this.mc.thePlayer == null) {
            return;
        }

        boolean leftHandMode = BetterWithHandsOptions.LEFT_HAND_MODE.value;

        if (!leftHandMode) {
            betterWithHands$renderOffhand(partialTicks, false);
        }
    }

    @Unique
    private void betterWithHands$renderOffhand(float partialTicks, boolean leftHandMode) {
        ItemStack offhand = ((OffhandCapable) this.mc.thePlayer.inventory).betterWithHands$getOffhandItem();

        int currentItemId = offhand != null ? offhand.itemID : -1;
        int currentItemMeta = offhand != null ? offhand.getMetadata() : -1;

        boolean itemTypeChanged = currentItemId != this.betterWithHands$lastOffhandItemId ||
                                   currentItemMeta != this.betterWithHands$lastOffhandItemMeta;

        if (itemTypeChanged) {
            this.betterWithHands$animationStartTime = System.currentTimeMillis();
            this.betterWithHands$lastOffhandItemId = currentItemId;
            this.betterWithHands$lastOffhandItemMeta = currentItemMeta;
            this.betterWithHands$animatingIn = offhand != null;
            if (offhand != null) {
                this.betterWithHands$lastRenderedOffhand = offhand.copy();
            }
        }

        long elapsed = System.currentTimeMillis() - this.betterWithHands$animationStartTime;
        float animFraction = Math.min(1.0F, (float) elapsed / EQUIP_ANIMATION_MS);

        float equipProgress;
        if (this.betterWithHands$animatingIn) {
            equipProgress = animFraction;
        } else {
            equipProgress = 1.0F - animFraction;
        }

        ItemStack itemToShow = offhand != null ? offhand : this.betterWithHands$lastRenderedOffhand;

        if (equipProgress <= 0.01F || itemToShow == null) {
            if (offhand == null) {
                this.betterWithHands$lastRenderedOffhand = null;
            }
            return;
        }

        ItemStack savedItem = this.itemToRender;
        float savedHeight = this.height;
        float savedOHeight = this.oHeight;

        float savedSwingProgress = this.mc.thePlayer.swingProgress;
        float savedPrevSwingProgress = this.mc.thePlayer.prevSwingProgress;

        float offhandSwing = ((OffhandSwingCapable) this.mc.thePlayer).betterWithHands$getOffhandSwingProgress(partialTicks);

        this.itemToRender = itemToShow;
        this.height = equipProgress;
        this.oHeight = equipProgress;
        this.mc.thePlayer.swingProgress = offhandSwing;
        this.mc.thePlayer.prevSwingProgress = offhandSwing;

        GLRenderer.pushFrame();

        if (!leftHandMode) {
            GLRenderer.setCullFace(CullFace.FRONT);
            GLRenderer.modelM4f().scale(-1.0F, 1.0F, 1.0F);
        }

        this.betterWithHands$renderingSecondHand = true;
        this.renderItemInFirstPerson(partialTicks);
        this.betterWithHands$renderingSecondHand = false;

        GLRenderer.popFrame();

        this.itemToRender = savedItem;
        this.height = savedHeight;
        this.oHeight = savedOHeight;
        this.mc.thePlayer.swingProgress = savedSwingProgress;
        this.mc.thePlayer.prevSwingProgress = savedPrevSwingProgress;

        if (offhand == null && equipProgress <= 0.01F) {
            this.betterWithHands$lastRenderedOffhand = null;
        }
    }
}
