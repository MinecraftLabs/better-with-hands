package com.sajmonoriginal.betterwithhands.mixin.server;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.sajmonoriginal.betterwithhands.util.HandAction;
import com.sajmonoriginal.betterwithhands.util.HandLogic;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.util.helper.Side;
import net.minecraft.core.world.pos.TilePos;
import net.minecraft.server.world.ServerPlayerController;
import net.minecraft.server.world.WorldServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = ServerPlayerController.class, remap = false)
public abstract class ServerPlayerControllerMixin {
    @Shadow
    public Player player;

    @Shadow
    private WorldServer thisWorld;

    @WrapMethod(method = "mineBlock")
    private boolean mineWithPreferredHand(int x, int y, int z, Side side, Operation<Boolean> original) {
        return HandLogic.perform(this.player, HandAction.MINE, this.thisWorld.getBlockType(new TilePos(x, y, z)),
            () -> original.call(x, y, z, side));
    }
}
