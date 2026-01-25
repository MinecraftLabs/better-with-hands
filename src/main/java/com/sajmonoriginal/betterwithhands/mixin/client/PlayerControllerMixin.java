package com.sajmonoriginal.betterwithhands.mixin.client;

import com.sajmonoriginal.betterwithhands.option.BetterWithHandsOptions;
import com.sajmonoriginal.betterwithhands.util.DynamicPriorityHelper;
import com.sajmonoriginal.betterwithhands.util.OffhandCapable;
import com.sajmonoriginal.betterwithhands.util.OffhandSwingCapable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.controller.PlayerController;
import net.minecraft.core.block.Block;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.util.helper.Side;
import net.minecraft.core.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(value = PlayerController.class, remap = false)
public abstract class PlayerControllerMixin {
    @Shadow
    @Final
    protected Minecraft mc;

    @Shadow
    protected int swingCooldown;

    @Unique
    private boolean betterWithHands$lastActionUsedOffhand = false;

    @Inject(method = "startDestroyBlock", at = @At("HEAD"))
    private void onStartDestroyBlock(int x, int y, int z, Side side, double xHit, double yHit, boolean repeat, CallbackInfo ci) {
        this.betterWithHands$lastActionUsedOffhand = shouldUseOffhandForMining();
    }

    @Inject(method = "continueDestroyBlock", at = @At("HEAD"))
    private void onContinueDestroyBlock(int x, int y, int z, Side side, double xHit, double yHit, CallbackInfo ci) {
        this.betterWithHands$lastActionUsedOffhand = shouldUseOffhandForMining();
    }

    @Inject(method = "useOrPlaceItemStackOnTile", at = @At("HEAD"), cancellable = true)
    private void onUseOrPlace(Player player, World world, ItemStack itemstack, int blockX, int blockY, int blockZ, Side side, double xPlaced, double yPlaced, CallbackInfoReturnable<Boolean> cir) {
        ItemStack offhand = ((OffhandCapable) player.inventory).betterWithHands$getOffhandItem();
        if (offhand != null && shouldUseOffhandForUse(itemstack)) {
            this.betterWithHands$lastActionUsedOffhand = true;
            
            boolean result = betterWithHands$useOrPlaceOffhand(player, world, offhand, blockX, blockY, blockZ, side, xPlaced, yPlaced);
            cir.setReturnValue(result);
        } else {
            this.betterWithHands$lastActionUsedOffhand = false;
        }
    }

    @Unique
    private boolean betterWithHands$useOrPlaceOffhand(Player player, World world, ItemStack offhandStack, int blockX, int blockY, int blockZ, Side side, double xPlaced, double yPlaced) {
        if (!player.getGamemode().canInteract()) {
            return false;
        }
        
        OffhandCapable inventory = (OffhandCapable) player.inventory;
        int blockId = world.getBlockId(blockX, blockY, blockZ);
        
        inventory.betterWithHands$setUsingOffhand(true);
        try {
            if (player.getHeldObject() == null) {
                if ((!player.isSneaking() || offhandStack == null) && blockId > 0 && 
                    net.minecraft.core.block.Blocks.blocksList[blockId].onBlockRightClicked(world, blockX, blockY, blockZ, player, side, xPlaced, yPlaced)) {
                    return true;
                }
                if (offhandStack == null) {
                    return false;
                }
                
                boolean used = offhandStack.useItem(player, world, blockX, blockY, blockZ, side, xPlaced, yPlaced);
                
                if (offhandStack.stackSize <= 0) {
                    inventory.betterWithHands$setOffhandItem(null);
                }
                
                return used;
            }
            return false;
        } finally {
            inventory.betterWithHands$setUsingOffhand(false);
        }
    }

    @Unique
    private boolean shouldUseOffhandForUse(ItemStack mainHandItem) {
        if (this.mc.thePlayer == null || this.mc.gameSettings == null) {
            return false;
        }

        ItemStack offhand = ((OffhandCapable) this.mc.thePlayer.inventory).betterWithHands$getOffhandItem();
        if (offhand == null) {
            return false;
        }

        BetterWithHandsOptions options = (BetterWithHandsOptions) this.mc.gameSettings;
        boolean dynamicPriority = options.betterWithHands$getDynamicHandPriority().value;
        
        if (dynamicPriority) {
            return DynamicPriorityHelper.shouldUseOffhandForAction(
                mainHandItem, offhand, DynamicPriorityHelper.ActionType.USE, null);
        }

        boolean swapPriority = options.betterWithHands$getSwapHandPriority().value;
        boolean useWhenEmpty = options.betterWithHands$getUseOffhandWhenMainEmpty().value;

        if (swapPriority) {
            return true;
        } else if (useWhenEmpty && mainHandItem == null) {
            return true;
        }
        return false;
    }

    @Inject(method = "swingItem", at = @At("HEAD"), cancellable = true)
    private void onSwingItem(boolean force, CallbackInfoReturnable<Boolean> cir) {
        if (this.mc.thePlayer == null) {
            return;
        }

        boolean useOffhand;
        boolean rightMouseDown = org.lwjgl.input.Mouse.isButtonDown(1);
        
        if (rightMouseDown) {
            useOffhand = shouldUseOffhandForUse(this.mc.thePlayer.inventory.getCurrentItem());
        } else {
            useOffhand = shouldUseOffhandForMining();
        }
        
        if (useOffhand) {
            if (this.swingCooldown == 0 || force) {
                ((OffhandSwingCapable) this.mc.thePlayer).betterWithHands$swingOffhand();
                this.swingCooldown = 5;
                cir.setReturnValue(true);
            } else {
                cir.setReturnValue(false);
            }
        }
    }

    @Unique
    private boolean shouldUseOffhandForMining() {
        if (this.mc.thePlayer == null || this.mc.gameSettings == null) {
            return false;
        }

        ItemStack offhand = ((OffhandCapable) this.mc.thePlayer.inventory).betterWithHands$getOffhandItem();
        if (offhand == null) {
            return false;
        }

        ItemStack mainHand = this.mc.thePlayer.inventory.getCurrentItem();
        
        BetterWithHandsOptions options = (BetterWithHandsOptions) this.mc.gameSettings;
        boolean dynamicPriority = options.betterWithHands$getDynamicHandPriority().value;
        
        if (dynamicPriority) {
            Block<?> targetBlock = getTargetBlock();
            if (targetBlock != null) {
                return DynamicPriorityHelper.shouldUseOffhandForAction(
                    mainHand, offhand, DynamicPriorityHelper.ActionType.MINING, targetBlock);
            }
        }

        boolean swapPriority = options.betterWithHands$getSwapHandPriority().value;
        boolean useWhenEmpty = options.betterWithHands$getUseOffhandWhenMainEmpty().value;

        if (swapPriority) {
            return true;
        } else if (useWhenEmpty && mainHand == null) {
            return true;
        }
        return false;
    }

    @Unique
    private Block<?> getTargetBlock() {
        if (this.mc.objectMouseOver != null && this.mc.thePlayer != null && this.mc.thePlayer.world != null) {
            int blockId = this.mc.thePlayer.world.getBlockId(this.mc.objectMouseOver.x, this.mc.objectMouseOver.y, this.mc.objectMouseOver.z);
            if (blockId > 0) {
                return net.minecraft.core.block.Blocks.blocksList[blockId];
            }
        }
        return null;
    }

    @Unique
    private boolean shouldUseOffhandForAttack() {
        if (this.mc.thePlayer == null || this.mc.gameSettings == null) {
            return false;
        }

        ItemStack offhand = ((OffhandCapable) this.mc.thePlayer.inventory).betterWithHands$getOffhandItem();
        if (offhand == null) {
            return false;
        }

        ItemStack mainHand = this.mc.thePlayer.inventory.getCurrentItem();
        
        if (DynamicPriorityHelper.isDualWieldingSwords(mainHand, offhand)) {
            return true;
        }
        
        BetterWithHandsOptions options = (BetterWithHandsOptions) this.mc.gameSettings;
        boolean dynamicPriority = options.betterWithHands$getDynamicHandPriority().value;
        
        if (dynamicPriority) {
            return DynamicPriorityHelper.shouldUseOffhandForAction(
                mainHand, offhand, DynamicPriorityHelper.ActionType.ATTACK, null);
        }

        boolean swapPriority = options.betterWithHands$getSwapHandPriority().value;
        boolean useWhenEmpty = options.betterWithHands$getUseOffhandWhenMainEmpty().value;

        if (swapPriority) {
            return true;
        } else if (useWhenEmpty && mainHand == null) {
            return true;
        }
        return false;
    }

}
