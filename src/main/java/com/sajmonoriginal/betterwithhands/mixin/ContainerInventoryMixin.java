package com.sajmonoriginal.betterwithhands.mixin;

import com.mojang.nbt.tags.CompoundTag;
import com.mojang.nbt.tags.ListTag;
import com.sajmonoriginal.betterwithhands.util.HandPriority;
import com.sajmonoriginal.betterwithhands.util.OffhandCapable;
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

@Mixin(value = ContainerInventory.class, remap = false)
public abstract class ContainerInventoryMixin implements OffhandCapable {
    @Unique
    private static final byte OFFHAND_NBT_SLOT = (byte) 150;

    @Shadow
    public Player player;

    @Shadow
    public ItemStack[] mainInventory;

    @Shadow
    public abstract int getCurrentSlot();

    @Shadow
    public abstract boolean currentSlotLocked();

    @Unique
    private ItemStack betterWithHands$offhandItem = null;

    @Unique
    private int betterWithHands$actionSlot = -1;

    @Unique
    private boolean betterWithHands$offhandEnabled = true;

    @Unique
    private HandPriority betterWithHands$handPriority = HandPriority.DEFAULT;

    @Override
    public ItemStack betterWithHands$getOffhandItem() {
        return this.betterWithHands$offhandItem;
    }

    @Override
    public void betterWithHands$setOffhandItem(ItemStack stack) {
        this.betterWithHands$offhandItem = stack;
    }

    @Override
    public void betterWithHands$swapHands() {
        if (!this.betterWithHands$offhandEnabled || this.currentSlotLocked() || this.betterWithHands$actionSlot >= 0) {
            return;
        }
        int slot = this.getCurrentSlot();
        ItemStack mainHand = this.mainInventory[slot];
        this.mainInventory[slot] = this.betterWithHands$offhandItem;
        this.betterWithHands$offhandItem = mainHand;
    }

    @Override
    public boolean betterWithHands$beginOffhandAction() {
        if (this.betterWithHands$actionSlot >= 0 || this.betterWithHands$offhandItem == null) {
            return false;
        }
        int slot = this.getCurrentSlot();
        ItemStack mainHand = this.mainInventory[slot];
        this.mainInventory[slot] = this.betterWithHands$offhandItem;
        this.betterWithHands$offhandItem = mainHand;
        this.betterWithHands$actionSlot = slot;
        return true;
    }

    @Override
    public void betterWithHands$endOffhandAction() {
        int slot = this.betterWithHands$actionSlot;
        if (slot < 0) {
            return;
        }
        ItemStack used = this.mainInventory[slot];
        this.mainInventory[slot] = this.betterWithHands$offhandItem;
        this.betterWithHands$offhandItem = used != null && used.stackSize <= 0 ? null : used;
        this.betterWithHands$actionSlot = -1;
    }

    @Override
    public boolean betterWithHands$isOffhandActionActive() {
        return this.betterWithHands$actionSlot >= 0;
    }

    @Override
    public boolean betterWithHands$isOffhandEnabled() {
        return this.betterWithHands$offhandEnabled;
    }

    @Override
    public void betterWithHands$setOffhandEnabled(boolean enabled) {
        this.betterWithHands$offhandEnabled = enabled;
    }

    @Override
    public HandPriority betterWithHands$getHandPriority() {
        return this.betterWithHands$handPriority;
    }

    @Override
    public void betterWithHands$setHandPriority(HandPriority priority) {
        this.betterWithHands$handPriority = priority;
    }

    @Inject(method = "insertItem", at = @At("HEAD"))
    private void mergeIntoOffhand(ItemStack stackToAdd, boolean useHotbarOffset, CallbackInfo ci) {
        ItemStack offhand = this.betterWithHands$offhandItem;
        if (offhand == null || !this.betterWithHands$offhandEnabled || this.betterWithHands$actionSlot >= 0 || stackToAdd.stackSize <= 0
            || !this.player.getGamemode().canInteract() || !offhand.canStackWith(stackToAdd)) {
            return;
        }
        int maxStack = Math.min(offhand.getMaxStackSize(), stackToAdd.getMaxStackSize());
        int toAdd = Math.min(maxStack - offhand.stackSize, stackToAdd.stackSize);
        if (toAdd > 0) {
            offhand.stackSize += toAdd;
            stackToAdd.stackSize -= toAdd;
            offhand.animationsToGo = 5;
        }
    }

    @Inject(method = "containsItem", at = @At("RETURN"), cancellable = true)
    private void checkOffhandContains(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValueZ() && this.betterWithHands$offhandItem != null && this.betterWithHands$offhandItem.isStackEqual(stack)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "transferAllContents", at = @At("TAIL"))
    private void transferOffhand(ContainerInventory inventory, CallbackInfo ci) {
        OffhandCapable previous = (OffhandCapable) inventory;
        this.betterWithHands$offhandItem = previous.betterWithHands$getOffhandItem();
        this.betterWithHands$handPriority = previous.betterWithHands$getHandPriority();
        previous.betterWithHands$setOffhandItem(null);
    }

    @Inject(method = "decrementAnimations", at = @At("TAIL"))
    private void updateOffhandAnimation(CallbackInfo ci) {
        if (this.betterWithHands$offhandItem != null && this.player.world != null) {
            this.betterWithHands$offhandItem.updateAnimation(this.player.world, this.player, 40, false);
        }
    }

    @Inject(method = "dropAllItems", at = @At("TAIL"))
    private void dropOffhand(CallbackInfo ci) {
        if (this.betterWithHands$offhandItem != null) {
            this.player.dropPlayerItem(this.betterWithHands$offhandItem);
            this.betterWithHands$offhandItem = null;
        }
    }

    @Inject(method = "save", at = @At("TAIL"))
    private void saveOffhand(ListTag listTag, CallbackInfoReturnable<ListTag> cir) {
        if (this.betterWithHands$offhandItem != null) {
            CompoundTag tag = new CompoundTag();
            tag.putByte("Slot", OFFHAND_NBT_SLOT);
            this.betterWithHands$offhandItem.writeToNBT(tag);
            listTag.addTag(tag);
        }
    }

    @Inject(method = "load", at = @At("TAIL"))
    private void loadOffhand(ListTag listTag, CallbackInfo ci) {
        this.betterWithHands$offhandItem = null;
        for (int i = 0; i < listTag.tagCount(); i++) {
            CompoundTag tag = (CompoundTag) listTag.tagAt(i);
            if (tag.getByte("Slot") == OFFHAND_NBT_SLOT) {
                this.betterWithHands$offhandItem = ItemStack.readItemStackFromNbt(tag);
            }
        }
    }
}
