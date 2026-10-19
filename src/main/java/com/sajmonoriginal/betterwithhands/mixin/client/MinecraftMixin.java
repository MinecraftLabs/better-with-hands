package com.sajmonoriginal.betterwithhands.mixin.client;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.sajmonoriginal.betterwithhands.client.ClientHands;
import com.sajmonoriginal.betterwithhands.option.BetterWithHandsOptions;
import com.sajmonoriginal.betterwithhands.util.HandAction;
import com.sajmonoriginal.betterwithhands.util.HandLogic;
import com.sajmonoriginal.betterwithhands.util.OffhandCapable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.InputDevice;
import net.minecraft.core.block.Block;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.util.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(value = Minecraft.class, remap = false)
public abstract class MinecraftMixin {
    @WrapMethod(method = "clickMouse")
    private void clickWithPreferredHand(int clickType, boolean attack, boolean repeat, Operation<Void> original) {
        Minecraft mc = (Minecraft) (Object) this;
        Player player = mc.thePlayer;
        if (player == null || !ClientHands.isEnabled()) {
            original.call(clickType, attack, repeat);
            return;
        }

        boolean dualWielding = player.getHeldObject() == null && HandLogic.isDualWieldingSwords(
            player.inventory.getCurrentItem(), ((OffhandCapable) player.inventory).betterWithHands$getOffhandItem());
        if (dualWielding && mc.objectMouseOver instanceof HitResult.Entity hit) {
            if (!attack) {
                return;
            }
            if (clickType == 0) {
                ClientHands.attackWithOffhand(mc, player, hit.entity);
            } else {
                mc.playerController.swingItem(true);
                mc.playerController.attack(player, hit.entity);
            }
            return;
        }

        HandAction action = HandAction.USE;
        Block<?> block = null;
        if (clickType == 0) {
            if (mc.objectMouseOver instanceof HitResult.Tile hit) {
                action = HandAction.MINE;
                block = mc.currentWorld.getBlockType(hit.tilePos);
            } else {
                action = mc.objectMouseOver instanceof HitResult.Entity ? HandAction.ATTACK : HandAction.SWING;
            }
        }
        HandLogic.perform(player, action, block, () -> {
            original.call(clickType, attack, repeat);
        });
    }

    @Inject(method = "checkBoundInputs", at = @At("HEAD"), cancellable = true)
    private void handleHandKeys(InputDevice device, CallbackInfoReturnable<Boolean> cir) {
        Player player = ((Minecraft) (Object) this).thePlayer;
        if (player == null) {
            return;
        }
        if (BetterWithHandsOptions.KEY_SWAP_HANDS.isPressEvent(device)) {
            ClientHands.swapHands(player);
            cir.setReturnValue(true);
        } else if (BetterWithHandsOptions.KEY_SWAP_PRIORITY.isPressEvent(device)) {
            ClientHands.toggleSwapPriority();
            cir.setReturnValue(true);
        }
    }
}
