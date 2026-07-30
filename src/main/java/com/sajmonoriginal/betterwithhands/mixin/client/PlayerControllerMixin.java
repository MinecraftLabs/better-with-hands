package com.sajmonoriginal.betterwithhands.mixin.client;

import com.sajmonoriginal.betterwithhands.BetterWithHandsMod;
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
import net.minecraft.core.net.packet.PacketCustomPayload;
import net.minecraft.core.util.helper.Side;
import net.minecraft.core.util.phys.HitResult;
import net.minecraft.core.world.World;
import net.minecraft.core.world.pos.TilePosc;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

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
    private void onStartDestroyBlock(TilePosc pos, Side side, double xHit, double yHit, boolean repeat, CallbackInfo ci) {
        this.betterWithHands$lastActionUsedOffhand = shouldUseOffhandForMining();
    }

    @Inject(method = "continueDestroyBlock", at = @At("HEAD"))
    private void onContinueDestroyBlock(TilePosc pos, Side side, double xHit, double yHit, CallbackInfo ci) {
        this.betterWithHands$lastActionUsedOffhand = shouldUseOffhandForMining();
    }

    @Inject(method = "useOrPlaceItemStackOnTile", at = @At("HEAD"), cancellable = true)
    private void onUseOrPlace(Player player, World world, ItemStack itemstack, TilePosc pos, Side side, double xPlaced, double yPlaced, CallbackInfoReturnable<Boolean> cir) {
        ItemStack offhand = ((OffhandCapable) player.inventory).betterWithHands$getOffhandItem();
        if (offhand != null && shouldUseOffhandForUse(itemstack)) {
            this.betterWithHands$lastActionUsedOffhand = true;

            boolean result = betterWithHands$useOrPlaceOffhand(player, world, offhand, pos, side, xPlaced, yPlaced);
            cir.setReturnValue(result);
        } else {
            this.betterWithHands$lastActionUsedOffhand = false;
        }
    }

    @Unique
    private boolean betterWithHands$useOrPlaceOffhand(Player player, World world, ItemStack offhandStack, TilePosc pos, Side side, double xPlaced, double yPlaced) {
        if (!player.getGamemode().canInteract()) {
            return false;
        }

        OffhandCapable inventory = (OffhandCapable) player.inventory;
        int blockX = pos.x();
        int blockY = pos.y();
        int blockZ = pos.z();
        int blockId = world.getBlockId(blockX, blockY, blockZ);

        if (player.getHeldObject() != null) {
            return false;
        }

        if ((!player.isSneaking() || offhandStack == null) && blockId > 0 &&
            net.minecraft.core.block.Blocks.blocksList[blockId].onInteracted(world, pos, player, side, xPlaced, yPlaced)) {
            return true;
        }
        if (offhandStack == null) {
            return false;
        }

        if (this.mc.isMultiplayerWorld()) {
            ((OffhandSwingCapable) player).betterWithHands$swingOffhand();
            betterWithHands$sendOffhandSwingPacket();
            betterWithHands$sendUseOffhandPacket(blockX, blockY, blockZ, side, xPlaced, yPlaced);
            return true;
        }

        inventory.betterWithHands$setUsingOffhand(true);
        try {
            boolean used = offhandStack.useItem(player, world, blockX, blockY, blockZ, side, xPlaced, yPlaced);

            if (used) {
                ((OffhandSwingCapable) player).betterWithHands$swingOffhand();
            }

            if (offhandStack.stackSize <= 0) {
                inventory.betterWithHands$setOffhandItem(null);
            }

            return used;
        } finally {
            inventory.betterWithHands$setUsingOffhand(false);
        }
    }

    @Unique
    private boolean shouldUseOffhandForUse(ItemStack mainHandItem) {
        if (this.mc.thePlayer == null) {
            return false;
        }

        ItemStack offhand = ((OffhandCapable) this.mc.thePlayer.inventory).betterWithHands$getOffhandItem();
        if (offhand == null) {
            return false;
        }

        boolean dynamicPriority = BetterWithHandsOptions.DYNAMIC_HAND_PRIORITY.value;

        if (dynamicPriority) {
            return DynamicPriorityHelper.shouldUseOffhandForAction(
                mainHandItem, offhand, DynamicPriorityHelper.ActionType.USE, null);
        }

        boolean swapPriority = BetterWithHandsOptions.SWAP_HAND_PRIORITY.value;
        boolean useWhenEmpty = BetterWithHandsOptions.USE_OFFHAND_WHEN_MAIN_EMPTY.value;

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

        ItemStack mainHand = this.mc.thePlayer.inventory.getCurrentItem();
        ItemStack offHand = ((OffhandCapable) this.mc.thePlayer.inventory).betterWithHands$getOffhandItem();

        if (DynamicPriorityHelper.isDualWieldingSwords(mainHand, offHand)) {
            boolean rightMouseDown = org.lwjgl.input.Mouse.isButtonDown(1);
            if (rightMouseDown) {
                return;
            }
            if (this.swingCooldown == 0 || force) {
                ((OffhandSwingCapable) this.mc.thePlayer).betterWithHands$swingOffhand();
                if (this.mc.isMultiplayerWorld()) {
                    betterWithHands$sendOffhandSwingPacket();
                }
                this.swingCooldown = 5;
                cir.setReturnValue(true);
            } else {
                cir.setReturnValue(false);
            }
            return;
        }

        boolean useOffhand;
        boolean rightMouseDown = org.lwjgl.input.Mouse.isButtonDown(1);

        if (rightMouseDown) {
            useOffhand = shouldUseOffhandForUse(mainHand);
        } else {
            useOffhand = shouldUseOffhandForMining();
        }

        if (useOffhand) {
            if (this.swingCooldown == 0 || force) {
                ((OffhandSwingCapable) this.mc.thePlayer).betterWithHands$swingOffhand();
                if (this.mc.isMultiplayerWorld()) {
                    betterWithHands$sendOffhandSwingPacket();
                }
                this.swingCooldown = 5;
                cir.setReturnValue(true);
            } else {
                cir.setReturnValue(false);
            }
        }
    }

    @Unique
    private boolean shouldUseOffhandForMining() {
        if (this.mc.thePlayer == null) {
            return false;
        }

        ItemStack offhand = ((OffhandCapable) this.mc.thePlayer.inventory).betterWithHands$getOffhandItem();
        if (offhand == null) {
            return false;
        }

        ItemStack mainHand = this.mc.thePlayer.inventory.getCurrentItem();

        boolean dynamicPriority = BetterWithHandsOptions.DYNAMIC_HAND_PRIORITY.value;

        if (dynamicPriority) {
            Block<?> targetBlock = getTargetBlock();
            if (targetBlock != null) {
                return DynamicPriorityHelper.shouldUseOffhandForAction(
                    mainHand, offhand, DynamicPriorityHelper.ActionType.MINING, targetBlock);
            }
        }

        boolean swapPriority = BetterWithHandsOptions.SWAP_HAND_PRIORITY.value;
        boolean useWhenEmpty = BetterWithHandsOptions.USE_OFFHAND_WHEN_MAIN_EMPTY.value;

        if (swapPriority) {
            return true;
        } else if (useWhenEmpty && mainHand == null) {
            return true;
        }
        return false;
    }

    @Unique
    private Block<?> getTargetBlock() {
        if (this.mc.objectMouseOver instanceof HitResult.Tile tileHit && this.mc.thePlayer != null && this.mc.thePlayer.world != null) {
            int blockId = this.mc.thePlayer.world.getBlockId(tileHit.tilePos.x(), tileHit.tilePos.y(), tileHit.tilePos.z());
            if (blockId > 0) {
                return net.minecraft.core.block.Blocks.blocksList[blockId];
            }
        }
        return null;
    }

    @Unique
    private boolean shouldUseOffhandForAttack() {
        if (this.mc.thePlayer == null) {
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

        boolean dynamicPriority = BetterWithHandsOptions.DYNAMIC_HAND_PRIORITY.value;

        if (dynamicPriority) {
            return DynamicPriorityHelper.shouldUseOffhandForAction(
                mainHand, offhand, DynamicPriorityHelper.ActionType.ATTACK, null);
        }

        boolean swapPriority = BetterWithHandsOptions.SWAP_HAND_PRIORITY.value;
        boolean useWhenEmpty = BetterWithHandsOptions.USE_OFFHAND_WHEN_MAIN_EMPTY.value;

        if (swapPriority) {
            return true;
        } else if (useWhenEmpty && mainHand == null) {
            return true;
        }
        return false;
    }

    @Unique
    private void betterWithHands$sendOffhandSwingPacket() {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            dos.writeByte(3);

            PacketCustomPayload packet = new PacketCustomPayload(BetterWithHandsMod.MOD_ID, baos.toByteArray());
            this.mc.getSendQueue().addToSendQueue(packet);
        } catch (Exception e) {
            BetterWithHandsMod.LOGGER.error("Failed to send offhand swing packet", e);
        }
    }

    @Unique
    private void betterWithHands$sendUseOffhandPacket(int blockX, int blockY, int blockZ, Side side, double xPlaced, double yPlaced) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            dos.writeByte(4);
            dos.writeInt(blockX);
            dos.writeInt(blockY);
            dos.writeInt(blockZ);
            dos.writeInt(side.id);
            dos.writeDouble(xPlaced);
            dos.writeDouble(yPlaced);

            PacketCustomPayload packet = new PacketCustomPayload(BetterWithHandsMod.MOD_ID, baos.toByteArray());
            this.mc.getSendQueue().addToSendQueue(packet);
        } catch (Exception e) {
            BetterWithHandsMod.LOGGER.error("Failed to send use offhand packet", e);
        }
    }
}
