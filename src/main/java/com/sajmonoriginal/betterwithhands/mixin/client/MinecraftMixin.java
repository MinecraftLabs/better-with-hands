package com.sajmonoriginal.betterwithhands.mixin.client;

import com.sajmonoriginal.betterwithhands.util.DynamicPriorityHelper;
import com.sajmonoriginal.betterwithhands.util.OffhandCapable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.util.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(value = Minecraft.class, remap = false)
public abstract class MinecraftMixin {

    @Inject(method = "clickMouse", at = @At("HEAD"), cancellable = true)
    private void onRightClickWithDualSwords(int button, boolean state, boolean hitBlock, CallbackInfo ci) {
        Minecraft mc = Minecraft.getMinecraft();
        Player player = mc.thePlayer;
        
        if (button != 1 || !state || player == null) {
            return;
        }
        
        ItemStack mainHand = player.inventory.getCurrentItem();
        ItemStack offHand = ((OffhandCapable) player.inventory).betterWithHands$getOffhandItem();
        
        if (!DynamicPriorityHelper.isDualWieldingSwords(mainHand, offHand)) {
            return;
        }
        
        HitResult hit = mc.objectMouseOver;
        if (hit != null && hit.hitType == HitResult.HitType.ENTITY) {
            Entity target = hit.entity;
            if (target != null) {
                player.attackTargetEntityWithCurrentItem(target);
                mc.playerController.swingItem(false);
                ci.cancel();
            }
        }
    }
}
