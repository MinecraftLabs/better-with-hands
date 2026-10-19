package com.sajmonoriginal.betterwithhands.client;

import com.sajmonoriginal.betterwithhands.mixin.client.accessor.PlayerControllerAccessor;
import com.sajmonoriginal.betterwithhands.network.HandPriorityMessage;
import com.sajmonoriginal.betterwithhands.network.OffhandAttackMessage;
import com.sajmonoriginal.betterwithhands.network.SwapHandsMessage;
import com.sajmonoriginal.betterwithhands.option.BetterWithHandsOptions;
import com.sajmonoriginal.betterwithhands.util.HandPriority;
import com.sajmonoriginal.betterwithhands.util.OffhandCapable;
import com.sajmonoriginal.betterwithhands.util.OffhandScope;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.hud.component.HudComponents;
import net.minecraft.client.option.GameSettings;
import net.minecraft.client.option.OptionBoolean;
import net.minecraft.client.world.WorldClientMP;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.entity.player.Player;
import org.jetbrains.annotations.Nullable;
import turniplabs.halplibe.helper.network.NetworkHandler;

@Environment(EnvType.CLIENT)
public final class ClientHands {
    private static boolean serverSupported = false;
    @Nullable
    private static HandPriority sentPriority = null;

    private ClientHands() {
    }

    public static void onLogin() {
        serverSupported = false;
        sentPriority = null;
    }

    public static void onServerHandshake() {
        serverSupported = true;
    }

    public static boolean isEnabled() {
        return !Minecraft.getMinecraft().isMultiplayerWorld() || serverSupported;
    }

    public static void syncHandPriority(Player player) {
        HandPriority priority = BetterWithHandsOptions.handPriority();
        OffhandCapable hands = (OffhandCapable) player.inventory;
        hands.betterWithHands$setOffhandEnabled(isEnabled());
        hands.betterWithHands$setHandPriority(priority);
        if (Minecraft.getMinecraft().isMultiplayerWorld() && serverSupported && !priority.equals(sentPriority)) {
            sentPriority = priority;
            NetworkHandler.sendToServer(new HandPriorityMessage(priority));
        }
    }

    public static void swapHands(Player player) {
        if (!isEnabled()) {
            return;
        }
        Minecraft mc = Minecraft.getMinecraft();
        ((OffhandCapable) player.inventory).betterWithHands$swapHands();
        if (mc.isMultiplayerWorld()) {
            ((PlayerControllerAccessor) mc.playerController).betterWithHands$syncCurrentPlayItem();
            NetworkHandler.sendToServer(new SwapHandsMessage());
        }
    }

    public static void toggleSwapPriority() {
        OptionBoolean option = BetterWithHandsOptions.SWAP_HAND_PRIORITY;
        option.set(!option.value);
        option.onUpdate();
        GameSettings.saveOptions();
        HudComponents.DYNAMIC_TOOLTIP.setOptionNameAndValueString(option);
    }

    public static void attackWithOffhand(Minecraft mc, Player player, Entity target) {
        OffhandScope.run(player, () -> mc.playerController.swingItem(true));
        if (mc.isMultiplayerWorld()) {
            ((PlayerControllerAccessor) mc.playerController).betterWithHands$syncCurrentPlayItem();
            NetworkHandler.sendToServer(new OffhandAttackMessage(target.id));
        }
        OffhandScope.run(player, () -> player.attackTargetEntityWithCurrentItem(target));
    }

    @Nullable
    public static Player findPlayer(int entityId) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer != null && mc.thePlayer.id == entityId) {
            return mc.thePlayer;
        }
        return findRemotePlayer(entityId);
    }

    @Nullable
    public static Player findRemotePlayer(int entityId) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.currentWorld instanceof WorldClientMP world && world.getEntityFromId(entityId) instanceof Player player && player != mc.thePlayer) {
            return player;
        }
        return null;
    }
}
