package com.sajmonoriginal.betterwithhands.mixin.client;

import com.sajmonoriginal.betterwithhands.util.OffhandCapable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.container.ScreenContainerAbstract;
import net.minecraft.client.gui.container.ScreenInventoryCreative;
import net.minecraft.client.render.renderer.BlendFactor;
import net.minecraft.client.render.renderer.GLRenderer;
import net.minecraft.client.render.renderer.State;
import net.minecraft.client.render.tessellator.TessellatorGeneral;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.player.inventory.menu.MenuAbstract;
import net.minecraft.core.player.inventory.menu.MenuInventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(value = ScreenInventoryCreative.class, remap = false)
public abstract class GuiInventoryCreativeMixin extends ScreenContainerAbstract {

    protected GuiInventoryCreativeMixin(MenuAbstract menu) {
        super(menu);
    }

    @Unique
    private int guiLeft;
    @Unique
    private int guiTop;

    @Inject(method = "init", at = @At("TAIL"))
    private void calculateGuiPosition(CallbackInfo ci) {
        this.guiLeft = (this.width - this.xSize) / 2;
        this.guiTop = (this.height - this.ySize) / 2;
    }

    @Inject(method = "drawGuiContainerBackgroundLayer", at = @At("TAIL"))
    private void drawOffhandSlotBackground(float partialTicks, CallbackInfo ci) {
        int slotX = this.guiLeft + 151;
        int slotY = this.guiTop + 61;

        GLRenderer.setColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.textureManager.loadTexture("/assets/minecraft/textures/gui/container/inventory.png").bind();
        this.drawTexturedModalRect(slotX, slotY, 7, 7, 18, 18);

        MenuInventory menu = (MenuInventory) this.inventorySlots;
        ItemStack offhand = ((OffhandCapable) menu.inventory).betterWithHands$getOffhandItem();

        if (offhand == null) {
            GLRenderer.enableState(State.BLEND);
            GLRenderer.setBlendFunc(BlendFactor.SRC_ALPHA, BlendFactor.ONE_MINUS_SRC_ALPHA);
            this.mc.textureManager.loadTexture("/assets/better_with_hands/textures/item/offhand_slot_outline.png").bind();
            betterWithHands$drawTexturedRect(slotX + 1, slotY + 1, 16, 16);
            GLRenderer.disableState(State.BLEND);
        }
    }

    @Unique
    private void betterWithHands$drawTexturedRect(int x, int y, int width, int height) {
        TessellatorGeneral t = GLRenderer.getTessellator();
        t.startDrawingQuads();
        t.addVertexWithUV((double)x, (double)(y + height), 0.0, 0.0, 1.0);
        t.addVertexWithUV((double)(x + width), (double)(y + height), 0.0, 1.0, 1.0);
        t.addVertexWithUV((double)(x + width), (double)y, 0.0, 1.0, 0.0);
        t.addVertexWithUV((double)x, (double)y, 0.0, 0.0, 0.0);
        t.draw();
    }
}
