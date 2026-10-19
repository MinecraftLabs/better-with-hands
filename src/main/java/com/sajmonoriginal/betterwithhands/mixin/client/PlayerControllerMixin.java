package com.sajmonoriginal.betterwithhands.mixin.client;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.sajmonoriginal.betterwithhands.util.HandAction;
import com.sajmonoriginal.betterwithhands.util.HandLogic;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.controller.PlayerController;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.util.helper.Side;
import net.minecraft.core.world.World;
import net.minecraft.core.world.pos.TilePosc;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Environment(EnvType.CLIENT)
@Mixin(value = PlayerController.class, remap = false)
public abstract class PlayerControllerMixin {
    @Shadow
    @Final
    protected Minecraft mc;

    @WrapMethod(method = "continueDestroyBlock")
    private void mineWithPreferredHand(TilePosc tilePos, Side side, double xHit, double yHit, Operation<Void> original) {
        Player player = this.mc.thePlayer;
        World world = this.mc.currentWorld;
        if (player == null || world == null) {
            original.call(tilePos, side, xHit, yHit);
            return;
        }
        HandLogic.perform(player, HandAction.MINE, world.getBlockType(tilePos), () -> {
            original.call(tilePos, side, xHit, yHit);
        });
    }
}
