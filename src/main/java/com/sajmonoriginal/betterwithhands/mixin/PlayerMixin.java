package com.sajmonoriginal.betterwithhands.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.sajmonoriginal.betterwithhands.util.HandAction;
import com.sajmonoriginal.betterwithhands.util.HandLogic;
import com.sajmonoriginal.betterwithhands.util.OffhandScope;
import com.sajmonoriginal.betterwithhands.util.OffhandSwingCapable;
import net.minecraft.core.block.Block;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Player.class, remap = false)
public abstract class PlayerMixin implements OffhandSwingCapable {
    @Unique
    private static final int SWING_DURATION = 8;

    @Unique
    private boolean betterWithHands$offhandSwinging = false;

    @Unique
    private int betterWithHands$offhandSwingTicks = 0;

    @Unique
    private float betterWithHands$offhandSwingProgress = 0.0F;

    @Unique
    private float betterWithHands$prevOffhandSwingProgress = 0.0F;

    @Override
    public void betterWithHands$swingOffhand() {
        this.betterWithHands$offhandSwingTicks = -1;
        this.betterWithHands$offhandSwinging = true;
    }

    @Override
    public boolean betterWithHands$isSwingingOffhand() {
        return this.betterWithHands$offhandSwinging;
    }

    @Override
    public float betterWithHands$getOffhandSwingProgress(float partialTick) {
        float delta = this.betterWithHands$offhandSwingProgress - this.betterWithHands$prevOffhandSwingProgress;
        if (delta < 0.0F) {
            delta += 1.0F;
        }
        return this.betterWithHands$prevOffhandSwingProgress + delta * partialTick;
    }

    @Inject(method = "updateAI", at = @At("TAIL"))
    private void updateOffhandSwing(CallbackInfo ci) {
        this.betterWithHands$prevOffhandSwingProgress = this.betterWithHands$offhandSwingProgress;
        if (this.betterWithHands$offhandSwinging) {
            this.betterWithHands$offhandSwingTicks++;
            if (this.betterWithHands$offhandSwingTicks >= SWING_DURATION) {
                this.betterWithHands$offhandSwingTicks = 0;
                this.betterWithHands$offhandSwinging = false;
            }
        } else {
            this.betterWithHands$offhandSwingTicks = 0;
        }
        this.betterWithHands$offhandSwingProgress = (float) this.betterWithHands$offhandSwingTicks / SWING_DURATION;
    }

    @Inject(method = "swingItem", at = @At("HEAD"), cancellable = true)
    private void swingOffhandInScope(CallbackInfo ci) {
        if (OffhandScope.isActive((Player) (Object) this)) {
            this.betterWithHands$swingOffhand();
            ci.cancel();
        }
    }

    @WrapMethod(method = "attackTargetEntityWithCurrentItem")
    private void attackWithPreferredHand(Entity entity, Operation<Void> original) {
        HandLogic.perform((Player) (Object) this, HandAction.ATTACK, null, () -> {
            original.call(entity);
        });
    }

    @WrapMethod(method = "useCurrentItemOnEntity")
    private boolean useOnEntityWithPreferredHand(Entity entity, Operation<Boolean> original) {
        return HandLogic.perform((Player) (Object) this, HandAction.USE, null, () -> original.call(entity));
    }

    @WrapMethod(method = "dropCurrentItem")
    private void dropFromPreferredHand(boolean dropFullStack, Operation<Void> original) {
        HandLogic.perform((Player) (Object) this, HandAction.DROP, null, () -> {
            original.call(dropFullStack);
        });
    }

    @WrapMethod(method = "getCurrentPlayerStrVsBlock")
    private float mineWithPreferredHand(Block<?> block, Operation<Float> original) {
        return HandLogic.perform((Player) (Object) this, HandAction.MINE, block, () -> original.call(block));
    }

    @WrapMethod(method = "canHarvestBlock")
    private boolean harvestWithPreferredHand(Block<?> block, Operation<Boolean> original) {
        return HandLogic.perform((Player) (Object) this, HandAction.MINE, block, () -> original.call(block));
    }
}
