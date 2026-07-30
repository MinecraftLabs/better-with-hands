package com.sajmonoriginal.betterwithhands.mixin.client;

import com.sajmonoriginal.betterwithhands.BetterWithHandsMod;
import com.sajmonoriginal.betterwithhands.option.BetterWithHandsOptions;
import com.sajmonoriginal.betterwithhands.util.OffhandCapable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.hud.HudIngame;
import net.minecraft.client.option.GameSettings;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.core.net.packet.PacketCustomPayload;
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
    private boolean wasSwapKeyPressed = false;
    @Unique
    private boolean wasSwapPriorityKeyPressed = false;
    @Unique
    private boolean betterWithHands$settingsSynced = false;

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

        KeyBinding swapHandsKey = BetterWithHandsOptions.KEY_SWAP_HANDS;
        boolean isSwapPressed = swapHandsKey.isPressed();

        if (isSwapPressed && !this.wasSwapKeyPressed) {
            ((OffhandCapable) this.mc.thePlayer.inventory).betterWithHands$swapHands();

            if (this.mc.isMultiplayerWorld()) {
                sendSwapPacket();
            }
        }

        this.wasSwapKeyPressed = isSwapPressed;

        KeyBinding swapPriorityKey = BetterWithHandsOptions.KEY_SWAP_PRIORITY;
        boolean isSwapPriorityPressed = swapPriorityKey.isPressed();

        if (isSwapPriorityPressed && !this.wasSwapPriorityKeyPressed) {
            BetterWithHandsOptions.SWAP_HAND_PRIORITY.value = !BetterWithHandsOptions.SWAP_HAND_PRIORITY.value;
            GameSettings.saveOptions();

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
            boolean swapPriority = BetterWithHandsOptions.SWAP_HAND_PRIORITY.value;
            boolean useWhenEmpty = BetterWithHandsOptions.USE_OFFHAND_WHEN_MAIN_EMPTY.value;
            boolean dynamicPriority = BetterWithHandsOptions.DYNAMIC_HAND_PRIORITY.value;

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
}
