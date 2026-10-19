package com.sajmonoriginal.betterwithhands.mixin.client;

import com.sajmonoriginal.betterwithhands.util.OffhandCapable;
import com.sajmonoriginal.betterwithhands.util.OffhandScope;
import com.sajmonoriginal.betterwithhands.util.OffhandSwingCapable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.PlayerLocal;
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
    @Unique
    private static final long EQUIP_ANIMATION_MS = 300L;

    @Shadow
    private Minecraft mc;

    @Shadow
    private ItemStack itemToRender;

    @Shadow
    private float height;

    @Shadow
    private float oHeight;

    @Shadow
    public abstract void renderItemInFirstPerson(float partialTick);

    @Unique
    private boolean betterWithHands$renderingOffhand = false;

    @Unique
    private ItemStack betterWithHands$previousOffhand = null;

    @Unique
    private ItemStack betterWithHands$shownOffhand = null;

    @Unique
    private long betterWithHands$animationStart = 0L;

    @Unique
    private boolean betterWithHands$animatingIn = false;

    @Inject(method = {"resetEquippedProgress", "updateEquippedItem"}, at = @At("HEAD"), cancellable = true)
    private void keepMainHandDuringOffhandAction(CallbackInfo ci) {
        if (this.mc.thePlayer != null && OffhandScope.isActive(this.mc.thePlayer)) {
            ci.cancel();
        }
    }

    @Inject(method = "renderItemInFirstPerson", at = @At("RETURN"))
    private void renderOffhand(float partialTick, CallbackInfo ci) {
        PlayerLocal player = this.mc.thePlayer;
        if (this.betterWithHands$renderingOffhand || player == null || player.getHeldObject() != null) {
            return;
        }

        ItemStack offhand = ((OffhandCapable) player.inventory).betterWithHands$getOffhandItem();
        if (!betterWithHands$isSameItem(offhand, this.betterWithHands$previousOffhand)) {
            this.betterWithHands$animationStart = System.currentTimeMillis();
            this.betterWithHands$animatingIn = offhand != null;
        }
        this.betterWithHands$previousOffhand = offhand;
        if (offhand != null) {
            this.betterWithHands$shownOffhand = offhand;
        }

        float animation = Math.min(1.0F, (float) (System.currentTimeMillis() - this.betterWithHands$animationStart) / EQUIP_ANIMATION_MS);
        float equipProgress = this.betterWithHands$animatingIn ? animation : 1.0F - animation;
        ItemStack shown = this.betterWithHands$shownOffhand;
        if (equipProgress <= 0.01F || shown == null) {
            if (offhand == null) {
                this.betterWithHands$shownOffhand = null;
            }
            return;
        }

        ItemStack mainItem = this.itemToRender;
        float mainHeight = this.height;
        float mainOHeight = this.oHeight;
        float mainSwing = player.swingProgress;
        float mainPrevSwing = player.prevSwingProgress;
        float offhandSwing = ((OffhandSwingCapable) player).betterWithHands$getOffhandSwingProgress(partialTick);

        this.itemToRender = shown;
        this.height = equipProgress;
        this.oHeight = equipProgress;
        player.swingProgress = offhandSwing;
        player.prevSwingProgress = offhandSwing;

        GLRenderer.pushFrame();
        if (!player.isLeftHanded()) {
            GLRenderer.setCullFace(CullFace.FRONT);
        }
        GLRenderer.modelM4f().scale(-1.0F, 1.0F, 1.0F);
        this.betterWithHands$renderingOffhand = true;
        try {
            this.renderItemInFirstPerson(partialTick);
        } finally {
            this.betterWithHands$renderingOffhand = false;
            GLRenderer.popFrame();
            this.itemToRender = mainItem;
            this.height = mainHeight;
            this.oHeight = mainOHeight;
            player.swingProgress = mainSwing;
            player.prevSwingProgress = mainPrevSwing;
        }
    }

    @Unique
    private static boolean betterWithHands$isSameItem(ItemStack a, ItemStack b) {
        if (a == null || b == null) {
            return a == b;
        }
        return a.itemID == b.itemID && (a.isItemStackDamageable() || a.getMetadata() == b.getMetadata());
    }
}
