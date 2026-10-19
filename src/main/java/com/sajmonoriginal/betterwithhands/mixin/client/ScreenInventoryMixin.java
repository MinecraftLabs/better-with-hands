package com.sajmonoriginal.betterwithhands.mixin.client;

import com.sajmonoriginal.betterwithhands.slot.SlotOffhand;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.container.ScreenContainerAbstract;
import net.minecraft.client.gui.container.ScreenInventory;
import net.minecraft.client.gui.container.ScreenInventoryCreative;
import net.minecraft.client.render.renderer.GLRenderer;
import net.minecraft.core.player.inventory.menu.MenuAbstract;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(value = {ScreenInventory.class, ScreenInventoryCreative.class}, remap = false)
public abstract class ScreenInventoryMixin extends ScreenContainerAbstract {
    @Unique
    private static final String INVENTORY_TEXTURE = "/assets/minecraft/textures/gui/container/inventory.png";

    protected ScreenInventoryMixin(MenuAbstract menu) {
        super(menu);
    }

    @Inject(method = "drawGuiContainerBackgroundLayer", at = @At("TAIL"))
    private void drawOffhandSlotFrame(float partialTick, CallbackInfo ci) {
        SlotOffhand slot = SlotOffhand.find(this.inventorySlots);
        if (slot == null) {
            return;
        }
        int left = (this.width - this.xSize) / 2;
        int top = (this.height - this.ySize) / 2;
        GLRenderer.setColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.textureManager.loadTexture(INVENTORY_TEXTURE).bind();
        this.drawTexturedModalRect(left + slot.x - 1, top + slot.y - 1, 7, 7, 18, 18);
    }
}
