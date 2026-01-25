package com.sajmonoriginal.betterwithhands.slot;

import com.sajmonoriginal.betterwithhands.util.OffhandCapable;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.player.inventory.container.ContainerInventory;
import net.minecraft.core.player.inventory.slot.Slot;

public class SlotOffhand extends Slot {
    private final ContainerInventory inventory;

    public SlotOffhand(ContainerInventory inventory, int x, int y) {
        super(inventory, 0, x, y);
        this.inventory = inventory;
    }

    @Override
    public ItemStack getItemStack() {
        return ((OffhandCapable) this.inventory).betterWithHands$getOffhandItem();
    }

    @Override
    public void set(ItemStack itemStack) {
        ((OffhandCapable) this.inventory).betterWithHands$setOffhandItem(itemStack);
        this.setChanged();
    }

    @Override
    public ItemStack remove(int amount) {
        ItemStack offhand = ((OffhandCapable) this.inventory).betterWithHands$getOffhandItem();
        if (offhand == null) {
            return null;
        }
        
        ItemStack result;
        if (offhand.stackSize <= amount) {
            result = offhand;
            ((OffhandCapable) this.inventory).betterWithHands$setOffhandItem(null);
        } else {
            result = offhand.splitStack(amount);
            if (offhand.stackSize == 0) {
                ((OffhandCapable) this.inventory).betterWithHands$setOffhandItem(null);
            }
        }
        this.setChanged();
        return result;
    }

    @Override
    public boolean hasItem() {
        return ((OffhandCapable) this.inventory).betterWithHands$getOffhandItem() != null;
    }

    @Override
    public int getMaxStackSize() {
        return 64;
    }
}
