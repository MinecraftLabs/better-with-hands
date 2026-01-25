package com.sajmonoriginal.betterwithhands.mixin.client;

import com.sajmonoriginal.betterwithhands.BetterWithHandsMod;
import com.sajmonoriginal.betterwithhands.option.BetterWithHandsOptions;
import com.sajmonoriginal.betterwithhands.util.OffhandCapable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ItemElement;
import net.minecraft.client.gui.hud.HudIngame;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.net.packet.PacketCustomPayload;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

@Environment(EnvType.CLIENT)
@Mixin(value = HudIngame.class, remap = false)
public abstract class GuiIngameMixin extends Gui {
    @Shadow
    protected Minecraft mc;

    @Unique
    private static final int SLOT_SIZE = 22;

    @Unique
    private boolean wasSwapKeyPressed = false;
    @Unique
    private boolean wasSwapPriorityKeyPressed = false;
    @Unique
    private boolean betterWithHands$settingsSynced = false;
    
    @Unique
    private ItemElement betterWithHands$itemElement;
    
    @Unique
    private ItemStack betterWithHands$lastOffhand = null;
    @Unique
    private float betterWithHands$equipProgress = 0.0F;
    @Unique
    private float betterWithHands$prevEquipProgress = 0.0F;

    @Inject(method = "renderGameOverlay", at = @At("HEAD"))
    private void handleOffhandKeyInput(float partialTicks, boolean flag, int mouseX, int mouseY, CallbackInfo ci) {
        if (this.mc.currentScreen != null || this.mc.thePlayer == null) {
            this.wasSwapKeyPressed = false;
            this.wasSwapPriorityKeyPressed = false;
            this.betterWithHands$settingsSynced = false;
            return;
        }

        if (this.mc.isMultiplayerWorld() && !this.betterWithHands$settingsSynced) {
            sendSettingsPacket();
            this.betterWithHands$settingsSynced = true;
        }

        BetterWithHandsOptions options = (BetterWithHandsOptions) this.mc.gameSettings;
        
        KeyBinding swapHandsKey = options.betterWithHands$getKeySwapHands();
        boolean isSwapPressed = swapHandsKey.isPressed();
        
        if (isSwapPressed && !this.wasSwapKeyPressed) {
            ((OffhandCapable) this.mc.thePlayer.inventory).betterWithHands$swapHands();
            
            if (this.mc.isMultiplayerWorld()) {
                sendSwapPacket();
            }
        }
        
        this.wasSwapKeyPressed = isSwapPressed;

        KeyBinding swapPriorityKey = options.betterWithHands$getKeySwapPriority();
        boolean isSwapPriorityPressed = swapPriorityKey.isPressed();
        
        if (isSwapPriorityPressed && !this.wasSwapPriorityKeyPressed) {
            options.betterWithHands$getSwapHandPriority().value = !options.betterWithHands$getSwapHandPriority().value;
            this.mc.gameSettings.saveOptions();
            
            if (this.mc.isMultiplayerWorld()) {
                sendSettingsPacket();
            }
        }
        
        this.wasSwapPriorityKeyPressed = isSwapPriorityPressed;
    }

    @Unique
    private void sendSwapPacket() {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            dos.writeByte(0);
            
            PacketCustomPayload packet = new PacketCustomPayload(BetterWithHandsMod.MOD_ID, baos.toByteArray());
            this.mc.getSendQueue().addToSendQueue(packet);
        } catch (Exception e) {
            BetterWithHandsMod.LOGGER.error("Failed to send swap packet", e);
        }
    }

    @Unique
    private void sendSettingsPacket() {
        try {
            BetterWithHandsOptions options = (BetterWithHandsOptions) this.mc.gameSettings;
            boolean swapPriority = options.betterWithHands$getSwapHandPriority().value;
            boolean useWhenEmpty = options.betterWithHands$getUseOffhandWhenMainEmpty().value;
            boolean dynamicPriority = options.betterWithHands$getDynamicHandPriority().value;
            
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            dos.writeByte(1);
            dos.writeBoolean(swapPriority);
            dos.writeBoolean(useWhenEmpty);
            dos.writeBoolean(dynamicPriority);
            
            PacketCustomPayload packet = new PacketCustomPayload(BetterWithHandsMod.MOD_ID, baos.toByteArray());
            this.mc.getSendQueue().addToSendQueue(packet);
        } catch (Exception e) {
            BetterWithHandsMod.LOGGER.error("Failed to send settings packet", e);
        }
    }

    @Inject(method = "renderGameOverlay", at = @At("TAIL"))
    private void renderOffhandSlot(float partialTicks, boolean flag, int mouseX, int mouseY, CallbackInfo ci) {
        if (this.mc.thePlayer == null || !this.mc.gameSettings.immersiveMode.drawHotbar()) {
            return;
        }

        int screenWidth = this.mc.resolution.getScaledWidthScreenCoords();
        int screenHeight = this.mc.resolution.getScaledHeightScreenCoords();

        int hotbarX = screenWidth / 2 - 91;
        int hotbarY = screenHeight - 22;

        ItemStack offhand = ((OffhandCapable) this.mc.thePlayer.inventory).betterWithHands$getOffhandItem();
        
        boolean itemTypeChanged = !betterWithHands$isSameItemType(offhand, this.betterWithHands$lastOffhand);
        if (itemTypeChanged) {
            this.betterWithHands$prevEquipProgress = 0.0F;
            this.betterWithHands$equipProgress = 0.0F;
        }
        this.betterWithHands$lastOffhand = offhand != null ? offhand.copy() : null;
        
        float targetProgress = offhand != null ? 1.0F : 0.0F;
        this.betterWithHands$prevEquipProgress = this.betterWithHands$equipProgress;
        float speed = 0.015F;
        if (this.betterWithHands$equipProgress < targetProgress) {
            this.betterWithHands$equipProgress = Math.min(this.betterWithHands$equipProgress + speed, targetProgress);
        } else if (this.betterWithHands$equipProgress > targetProgress) {
            this.betterWithHands$equipProgress = Math.max(this.betterWithHands$equipProgress - speed, targetProgress);
        }
        
        float animProgress = this.betterWithHands$prevEquipProgress + (this.betterWithHands$equipProgress - this.betterWithHands$prevEquipProgress) * partialTicks;

        BetterWithHandsOptions options = (BetterWithHandsOptions) this.mc.gameSettings;
        boolean leftHandMode = options.betterWithHands$getLeftHandMode().value;
        
        int slotX;
        if (leftHandMode) {
            slotX = hotbarX + 182 - 2;
        } else {
            slotX = hotbarX - SLOT_SIZE - 4;
        }
        int baseSlotY = hotbarY;
        float slideOffset = (1.0F - animProgress) * 24.0F;
        int slotY = baseSlotY + (int) slideOffset;
        
        if (animProgress > 0.01F) {
            if (this.betterWithHands$itemElement == null) {
                this.betterWithHands$itemElement = new ItemElement(this.mc);
            }

            GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
            GL11.glColor4f(1.0F, 1.0F, 1.0F, animProgress);
            
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            
            this.mc.textureManager.loadTexture("/gui/gui.png").bind();
            if (leftHandMode) {
                betterWithHands$drawFlippedSlot(slotX, slotY, SLOT_SIZE, 22);
            } else {
                this.drawTexturedModalRect(slotX, slotY, 0, 0, SLOT_SIZE, 22);
            }

            if (offhand != null) {
                int itemX = slotX + 3;
                int itemY = slotY + 3;
                this.betterWithHands$itemElement.render(offhand, itemX, itemY);
            }
            
            GL11.glPopAttrib();
        }
        
        betterWithHands$renderPriorityIndicator(slotX, slotY, animProgress, hotbarX, hotbarY, leftHandMode);
    }

    @Unique
    private void betterWithHands$renderPriorityIndicator(int offhandSlotX, int offhandSlotY, float offhandAnimProgress, int hotbarX, int hotbarY, boolean leftHandMode) {
        BetterWithHandsOptions options = (BetterWithHandsOptions) this.mc.gameSettings;
        
        com.sajmonoriginal.betterwithhands.option.IndicatorType indicatorType = options.betterWithHands$getIndicatorType().value;
        if (indicatorType == com.sajmonoriginal.betterwithhands.option.IndicatorType.NONE) {
            return;
        }
        
        boolean swapPriority = options.betterWithHands$getSwapHandPriority().value;
        boolean useWhenEmpty = options.betterWithHands$getUseOffhandWhenMainEmpty().value;
        boolean showOnlyWithOffhand = options.betterWithHands$getShowIndicatorOnlyWithOffhand().value;
        
        ItemStack offhand = ((OffhandCapable) this.mc.thePlayer.inventory).betterWithHands$getOffhandItem();
        ItemStack mainhand = this.mc.thePlayer.inventory.getCurrentItem();
        
        boolean offhandActive = false;
        if (offhand != null) {
            if (swapPriority) {
                offhandActive = true;
            } else if (useWhenEmpty && mainhand == null) {
                offhandActive = true;
            }
        }
        
        if (showOnlyWithOffhand && offhand == null) {
            return;
        }
        
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        
        int screenWidth = this.mc.resolution.getScaledWidthScreenCoords();
        int screenHeight = this.mc.resolution.getScaledHeightScreenCoords();
        
        if (indicatorType == com.sajmonoriginal.betterwithhands.option.IndicatorType.BORDER) {
            this.mc.textureManager.loadTexture("/assets/betterwithhands/textures/gui/selector.png").bind();
            
            if (offhandActive && offhandAnimProgress > 0.01F) {
                betterWithHands$drawTexturedRect(offhandSlotX - 1, offhandSlotY - 1, 24, 24);
            } else if (!offhandActive) {
                int selectedSlot = this.mc.thePlayer.inventory.getCurrentItemIndex();
                int hotbarOffset = this.mc.thePlayer.inventory.getHotbarOffset();
                int relativeSlot = selectedSlot - hotbarOffset;
                int selectorX = screenWidth / 2 - 91 + relativeSlot * 20;
                int selectorY = screenHeight - 22 - 1;
                betterWithHands$drawTexturedRect(selectorX, selectorY, 24, 24);
            }
        } else if (indicatorType == com.sajmonoriginal.betterwithhands.option.IndicatorType.DOT) {
            this.mc.textureManager.loadTexture("/assets/betterwithhands/textures/gui/dot.png").bind();
            
            if (offhandActive && offhandAnimProgress > 0.01F) {
                int dotX = offhandSlotX + (SLOT_SIZE / 2) - 2;
                int dotY = offhandSlotY;
                betterWithHands$drawTexturedRect(dotX, dotY, 5, 3);
            } else if (!offhandActive) {
                int selectedSlot = this.mc.thePlayer.inventory.getCurrentItemIndex();
                int hotbarOffset = this.mc.thePlayer.inventory.getHotbarOffset();
                int relativeSlot = selectedSlot - hotbarOffset;
                int slotStartX = screenWidth / 2 - 91 + relativeSlot * 20;
                int dotX = slotStartX + 10;
                int dotY = screenHeight - 22;
                betterWithHands$drawTexturedRect(dotX, dotY, 5, 3);
            }
        } else if (indicatorType == com.sajmonoriginal.betterwithhands.option.IndicatorType.ARROW) {
            this.mc.textureManager.loadTexture("/assets/betterwithhands/textures/gui/priority_indicator.png").bind();
            
            if (offhandActive && offhandAnimProgress > 0.01F) {
                int arrowX = offhandSlotX + (SLOT_SIZE / 2) - 2;
                int arrowY = offhandSlotY + 2;
                betterWithHands$drawTexturedRect(arrowX, arrowY, 5, 5);
            } else if (!offhandActive) {
                int selectedSlot = this.mc.thePlayer.inventory.getCurrentItemIndex();
                int hotbarOffset = this.mc.thePlayer.inventory.getHotbarOffset();
                int relativeSlot = selectedSlot - hotbarOffset;
                int slotStartX = screenWidth / 2 - 91 + relativeSlot * 20;
                int arrowX = slotStartX + 9;
                int arrowY = screenHeight - 22 + 2;
                betterWithHands$drawTexturedRect(arrowX, arrowY, 5, 5);
            }
        }
        
        GL11.glPopAttrib();
    }

    @Unique
    private void betterWithHands$drawTexturedRect(int x, int y, int width, int height) {
        net.minecraft.client.render.tessellator.Tessellator t = net.minecraft.client.render.tessellator.Tessellator.instance;
        t.startDrawingQuads();
        t.addVertexWithUV(x, y + height, 300.0, 0.0, 1.0);
        t.addVertexWithUV(x + width, y + height, 300.0, 1.0, 1.0);
        t.addVertexWithUV(x + width, y, 300.0, 1.0, 0.0);
        t.addVertexWithUV(x, y, 300.0, 0.0, 0.0);
        t.draw();
    }

    @Unique
    private void betterWithHands$drawFlippedSlot(int x, int y, int width, int height) {
        net.minecraft.client.render.tessellator.Tessellator t = net.minecraft.client.render.tessellator.Tessellator.instance;
        float u0 = 0.0F;
        float u1 = (float) width / 256.0F;
        float v0 = 0.0F;
        float v1 = (float) height / 256.0F;
        t.startDrawingQuads();
        t.addVertexWithUV(x, y + height, 0.0, u1, v1);
        t.addVertexWithUV(x + width, y + height, 0.0, u0, v1);
        t.addVertexWithUV(x + width, y, 0.0, u0, v0);
        t.addVertexWithUV(x, y, 0.0, u1, v0);
        t.draw();
    }

    @Unique
    private boolean betterWithHands$isSameItemType(ItemStack a, ItemStack b) {
        if (a == null && b == null) {
            return true;
        }
        if (a == null || b == null) {
            return false;
        }
        return a.itemID == b.itemID && a.getMetadata() == b.getMetadata();
    }
}
