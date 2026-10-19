package com.sajmonoriginal.betterwithhands.mixin;

import com.mojang.nbt.tags.CompoundTag;
import com.mojang.nbt.tags.ListTag;
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
public abstract class InventoryPlayerMixin implements OffhandCapable {
    @Shadow
    public Player player;

    @Shadow
    public ItemStack[] mainInventory;

    @Shadow
    public ItemStack[] armorInventory;

    @Shadow
    public abstract int getCurrentSlot();

    @Unique
    private ItemStack offhandItem = null;

    @Unique
    private boolean usingOffhand = false;

    @Unique
    private boolean swapPriority = false;

    @Unique
    private boolean useWhenEmpty = true;

    @Unique
    private boolean dynamicPriority = false;

    @Override
    public ItemStack betterWithHands$getOffhandItem() {
        return this.offhandItem;
    }

    @Override
    public void betterWithHands$setOffhandItem(ItemStack stack) {
        this.offhandItem = stack;
    }

    @Override
    public void betterWithHands$swapHands() {
        int currentSlot = this.getCurrentSlot();
        ItemStack mainHand = this.mainInventory[currentSlot];
        this.mainInventory[currentSlot] = this.offhandItem;
        this.offhandItem = mainHand;
    }

    @Override
    public boolean betterWithHands$isUsingOffhand() {
        return this.usingOffhand;
    }

    @Override
    public void betterWithHands$setUsingOffhand(boolean using) {
        this.usingOffhand = using;
    }

    @Override
    public boolean betterWithHands$getSwapPriority() {
        return this.swapPriority;
    }

    @Override
    public void betterWithHands$setSwapPriority(boolean swapPriority) {
        this.swapPriority = swapPriority;
    }

    @Override
    public boolean betterWithHands$getUseWhenEmpty() {
        return this.useWhenEmpty;
    }

    @Override
    public void betterWithHands$setUseWhenEmpty(boolean useWhenEmpty) {
        this.useWhenEmpty = useWhenEmpty;
    }

    @Override
    public boolean betterWithHands$getDynamicPriority() {
        return this.dynamicPriority;
    }

    @Override
    public void betterWithHands$setDynamicPriority(boolean dynamicPriority) {
        this.dynamicPriority = dynamicPriority;
    }

    @Inject(method = "insertItem", at = @At("HEAD"))
    private void mergeIntoOffhand(ItemStack stackToAdd, boolean useHotbarOffset, CallbackInfo ci) {
        if (stackToAdd.stackSize <= 0 || !this.player.getGamemode().canInteract()) {
            return;
        }

        if (this.offhandItem != null && this.offhandItem.canStackWith(stackToAdd)) {
            int maxStack = Math.min(this.offhandItem.getMaxStackSize(), stackToAdd.getMaxStackSize());
            int canAdd = maxStack - this.offhandItem.stackSize;
            if (canAdd > 0) {
                int toAdd = Math.min(canAdd, stackToAdd.stackSize);
                this.offhandItem.stackSize += toAdd;
                stackToAdd.stackSize -= toAdd;
                this.offhandItem.animationsToGo = 5;
            }
        }
    }

    @Inject(method = "decrementAnimations", at = @At("TAIL"))
    private void updateOffhandAnimation(CallbackInfo ci) {
        if (this.offhandItem != null && this.player.world != null) {
            this.offhandItem.updateAnimation(this.player.world, this.player, 40, false);
        }
    }

    @Inject(method = "containsItem", at = @At("RETURN"), cancellable = true)
    private void checkOffhandContains(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValueZ() && this.offhandItem != null && this.offhandItem.isStackEqual(stack)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "transferAllContents", at = @At("TAIL"))
    private void transferOffhand(ContainerInventory inventory, CallbackInfo ci) {
        OffhandCapable previous = (OffhandCapable) inventory;
        this.offhandItem = previous.betterWithHands$getOffhandItem();
        previous.betterWithHands$setOffhandItem(null);
        this.swapPriority = previous.betterWithHands$getSwapPriority();
        this.useWhenEmpty = previous.betterWithHands$getUseWhenEmpty();
        this.dynamicPriority = previous.betterWithHands$getDynamicPriority();
    }

    @Inject(method = "dropAllItems", at = @At("TAIL"))
    private void dropOffhandOnDeath(CallbackInfo ci) {
        if (this.offhandItem != null) {
            this.player.dropPlayerItem(this.offhandItem);
            this.offhandItem = null;
        }
    }

    @Inject(method = "save", at = @At("TAIL"))
    private void saveOffhandToNBT(ListTag listTag, CallbackInfoReturnable<ListTag> cir) {
        if (this.offhandItem != null) {
            CompoundTag offhandTag = new CompoundTag();
            offhandTag.putByte("Slot", (byte) 150);
            this.offhandItem.writeToNBT(offhandTag);
            listTag.addTag(offhandTag);
        }
    }

    @Inject(method = "load", at = @At("TAIL"))
    private void loadOffhandFromNBT(ListTag listTag, CallbackInfo ci) {
        this.offhandItem = null;
        for (int i = 0; i < listTag.tagCount(); i++) {
            CompoundTag tag = (CompoundTag) listTag.tagAt(i);
            int slot = tag.getByte("Slot") & 255;
            if (slot == 150) {
                this.offhandItem = ItemStack.readItemStackFromNbt(tag);
            }
        }
    }
}
