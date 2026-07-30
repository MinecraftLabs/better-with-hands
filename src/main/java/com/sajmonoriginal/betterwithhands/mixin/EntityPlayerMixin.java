package com.sajmonoriginal.betterwithhands.mixin;

import com.sajmonoriginal.betterwithhands.util.DualWieldHelper;
import com.sajmonoriginal.betterwithhands.util.OffhandCapable;
import com.sajmonoriginal.betterwithhands.util.OffhandLogicHelper;
import com.sajmonoriginal.betterwithhands.util.OffhandSwingCapable;
import net.minecraft.core.block.Block;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.player.inventory.container.ContainerInventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = Player.class, remap = false)
public abstract class EntityPlayerMixin implements OffhandSwingCapable {
    @Shadow
    public ContainerInventory inventory;

    @Unique
    private boolean betterWithHands$offhandSwinging = false;
    @Unique
    private int betterWithHands$offhandSwingProgressInt = 0;
    @Unique
    private float betterWithHands$offhandSwingProgress = 0.0F;
    @Unique
    private float betterWithHands$offhandPrevSwingProgress = 0.0F;

    public ItemStack getOffhandItem() {
        return ((OffhandCapable) this.inventory).betterWithHands$getOffhandItem();
    }

    public void setOffhandItem(ItemStack stack) {
        ((OffhandCapable) this.inventory).betterWithHands$setOffhandItem(stack);
    }

    public void swapHands() {
        ((OffhandCapable) this.inventory).betterWithHands$swapHands();
    }

    @Override
    public void betterWithHands$swingOffhand() {
        this.betterWithHands$offhandSwinging = true;
        this.betterWithHands$offhandSwingProgressInt = 0;
    }

    @Override
    public float betterWithHands$getOffhandSwingProgress(float partialTicks) {
        float f = this.betterWithHands$offhandSwingProgress - this.betterWithHands$offhandPrevSwingProgress;
        if (f < 0.0F) {
            f += 1.0F;
        }
        return this.betterWithHands$offhandPrevSwingProgress + f * partialTicks;
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void updateOffhandSwing(CallbackInfo ci) {
        this.betterWithHands$offhandPrevSwingProgress = this.betterWithHands$offhandSwingProgress;

        if (this.betterWithHands$offhandSwinging) {
            this.betterWithHands$offhandSwingProgressInt++;
            if (this.betterWithHands$offhandSwingProgressInt >= 8) {
                this.betterWithHands$offhandSwingProgressInt = 0;
                this.betterWithHands$offhandSwinging = false;
            }
        } else {
            this.betterWithHands$offhandSwingProgressInt = 0;
        }

        this.betterWithHands$offhandSwingProgress = (float) this.betterWithHands$offhandSwingProgressInt / 8.0F;
    }

    @Shadow
    public abstract void dropItem(ItemStack itemStack, boolean randomChoice);

    @Inject(method = "attackTargetEntityWithCurrentItem", at = @At("HEAD"))
    private void onAttackStart(Entity target, CallbackInfo ci) {
        if (DualWieldHelper.shouldUseOffhandForAttack()) {
            ((OffhandCapable) this.inventory).betterWithHands$swapHands();
        }
    }

    @Inject(method = "attackTargetEntityWithCurrentItem", at = @At("RETURN"))
    private void onAttackEnd(Entity target, CallbackInfo ci) {
        if (DualWieldHelper.shouldUseOffhandForAttack()) {
            ((OffhandCapable) this.inventory).betterWithHands$swapHands();
            DualWieldHelper.clearUseOffhandForAttack();
        }
    }

    @Inject(method = "dropCurrentItem", at = @At("HEAD"), cancellable = true)
    private void onDropCurrentItem(boolean dropFullStack, CallbackInfo ci) {
        OffhandCapable offhandInventory = (OffhandCapable) this.inventory;
        ItemStack mainHand = this.inventory.getCurrentItem();
        ItemStack offhand = offhandInventory.betterWithHands$getOffhandItem();

        if (OffhandLogicHelper.shouldUseOffhand(mainHand, offhand, offhandInventory)) {
            if (offhand != null) {
                int dropAmount = dropFullStack ? offhand.stackSize : 1;
                ItemStack toDrop = offhand.copy();
                toDrop.stackSize = dropAmount;

                offhand.stackSize -= dropAmount;
                if (offhand.stackSize <= 0) {
                    offhandInventory.betterWithHands$setOffhandItem(null);
                }

                this.dropItem(toDrop, false);
            }
            ci.cancel();
        }
    }

    @Inject(method = "getCurrentEquippedItem", at = @At("HEAD"), cancellable = true)
    private void onGetCurrentEquippedItem(CallbackInfoReturnable<ItemStack> cir) {
        OffhandCapable offhandInventory = (OffhandCapable) this.inventory;
        if (offhandInventory.betterWithHands$isUsingOffhand()) {
            cir.setReturnValue(offhandInventory.betterWithHands$getOffhandItem());
        }
    }

    @Inject(method = "getCurrentPlayerStrVsBlock", at = @At("HEAD"), cancellable = true)
    private void onGetCurrentPlayerStrVsBlock(Block<?> block, CallbackInfoReturnable<Float> cir) {
        OffhandCapable offhandInventory = (OffhandCapable) this.inventory;
        ItemStack offhand = offhandInventory.betterWithHands$getOffhandItem();
        if (offhand == null) {
            return;
        }
        ItemStack mainHand = this.inventory.getCurrentItem();
        if (!OffhandLogicHelper.shouldUseOffhandForBlock(mainHand, offhand, offhandInventory, block)) {
            return;
        }
        cir.setReturnValue(offhand.getStrVsBlock(block));
    }

    @Inject(method = "canHarvestBlock", at = @At("HEAD"), cancellable = true)
    private void onCanHarvestBlock(Block<?> block, CallbackInfoReturnable<Boolean> cir) {
        if (block == null) {
            return;
        }
        OffhandCapable offhandInventory = (OffhandCapable) this.inventory;
        ItemStack offhand = offhandInventory.betterWithHands$getOffhandItem();
        if (offhand == null) {
            return;
        }
        ItemStack mainHand = this.inventory.getCurrentItem();
        if (!OffhandLogicHelper.shouldUseOffhandForBlock(mainHand, offhand, offhandInventory, block)) {
            return;
        }
        if (block.getMaterial().isAlwaysDestroyable()) {
            cir.setReturnValue(true);
            return;
        }
        cir.setReturnValue(offhand.canHarvestBlock((Player) (Object) this, block));
    }
}
