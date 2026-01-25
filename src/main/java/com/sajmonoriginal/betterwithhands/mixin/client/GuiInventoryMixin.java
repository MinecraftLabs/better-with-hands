package com.sajmonoriginal.betterwithhands.mixin.client;

import com.sajmonoriginal.betterwithhands.util.OffhandCapable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.container.ScreenContainerAbstract;
import net.minecraft.client.gui.container.ScreenInventory;
import net.minecraft.client.render.tessellator.Tessellator;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.player.inventory.menu.MenuAbstract;
import net.minecraft.core.player.inventory.menu.MenuInventory;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(value = ScreenInventory.class, remap = false)
public abstract class GuiInventoryMixin extends ScreenContainerAbstract {

    protected GuiInventoryMixin(MenuAbstract menu) {
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

        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.textureManager.loadTexture("/gui/inventory.png").bind();
        this.drawTexturedModalRect(slotX, slotY, 7, 7, 18, 18);

        MenuInventory menu = (MenuInventory) this.inventorySlots;
        ItemStack offhand = ((OffhandCapable) menu.inventory).betterWithHands$getOffhandItem();
        
        if (offhand == null) {
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            this.mc.textureManager.loadTexture("/assets/betterwithhands/textures/item/offhand_slot_outline.png").bind();
            betterWithHands$drawTexturedRect(slotX + 1, slotY + 1, 16, 16);
            GL11.glDisable(GL11.GL_BLEND);
        }
    }

    @Unique
    private void betterWithHands$drawTexturedRect(int x, int y, int width, int height) {
        Tessellator t = Tessellator.instance;
        t.startDrawingQuads();
        t.addVertexWithUV(x, y + height, 0.0, 0.0, 1.0);
        t.addVertexWithUV(x + width, y + height, 0.0, 1.0, 1.0);
        t.addVertexWithUV(x + width, y, 0.0, 1.0, 0.0);
        t.addVertexWithUV(x, y, 0.0, 0.0, 0.0);
        t.draw();
    }
}
