package com.sajmonoriginal.betterwithhands.mixin;

import com.mojang.nbt.tags.CompoundTag;
import com.mojang.nbt.tags.ListTag;
import com.sajmonoriginal.betterwithhands.util.OffhandCapable;
import com.sajmonoriginal.betterwithhands.util.OffhandLogicHelper;
import net.minecraft.core.block.Block;
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
    public abstract int getCurrentItemIndex();

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
        int currentSlot = this.getCurrentItemIndex();
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

    @Inject(method = "getStrVsBlock", at = @At("HEAD"), cancellable = true)
    private void onGetStrVsBlock(Block<?> block, CallbackInfoReturnable<Float> cir) {
        int currentSlot = this.getCurrentItemIndex();
        ItemStack mainHand = this.mainInventory[currentSlot];

        boolean useOffhand = OffhandLogicHelper.shouldUseOffhandForBlock(mainHand, this.offhandItem, this, block);

        if (useOffhand) {
            float strength = this.offhandItem.getStrVsBlock(block);
            cir.setReturnValue(strength);
        }
    }

    @Inject(method = "canHarvestBlock", at = @At("HEAD"), cancellable = true)
    private void onCanHarvestBlock(Block<?> block, CallbackInfoReturnable<Boolean> cir) {
        if (block == null) {
            return;
        }

        int currentSlot = this.getCurrentItemIndex();
        ItemStack mainHand = this.mainInventory[currentSlot];

        boolean useOffhand = OffhandLogicHelper.shouldUseOffhandForBlock(mainHand, this.offhandItem, this, block);

        if (useOffhand) {
            if (block.getMaterial().isAlwaysDestroyable()) {
                cir.setReturnValue(true);
                return;
            }
            cir.setReturnValue(this.offhandItem.canHarvestBlock(this.player, block));
        }
    }

    @Inject(method = "insertItem", at = @At("TAIL"))
    private void tryInsertIntoOffhand(ItemStack stackToAdd, boolean useHotbarOffset, CallbackInfo ci) {
        if (stackToAdd == null || stackToAdd.stackSize <= 0) {
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

    @Inject(method = "dropAllItems", at = @At("TAIL"))
    private void dropOffhandOnDeath(CallbackInfo ci) {
        if (this.offhandItem != null) {
            this.player.dropPlayerItem(this.offhandItem);
            this.offhandItem = null;
        }
    }

    @Inject(method = "writeToNBT", at = @At("TAIL"))
    private void saveOffhandToNBT(ListTag listTag, CallbackInfoReturnable<ListTag> cir) {
        if (this.offhandItem != null) {
            CompoundTag offhandTag = new CompoundTag();
            offhandTag.putByte("Slot", (byte) 150);
            this.offhandItem.writeToNBT(offhandTag);
            listTag.addTag(offhandTag);
        }
    }

    @Inject(method = "readFromNBT", at = @At("TAIL"))
    private void loadOffhandFromNBT(ListTag listTag, CallbackInfo ci) {
        for (int i = 0; i < listTag.tagCount(); i++) {
            CompoundTag tag = (CompoundTag) listTag.tagAt(i);
            int slot = tag.getByte("Slot") & 255;
            if (slot == 150) {
                this.offhandItem = ItemStack.readItemStackFromNbt(tag);
            }
        }
    }
}
