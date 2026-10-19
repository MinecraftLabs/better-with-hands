package com.sajmonoriginal.betterwithhands.slot;

import com.sajmonoriginal.betterwithhands.util.OffhandCapable;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.player.inventory.container.ContainerInventory;
import net.minecraft.core.player.inventory.menu.MenuAbstract;
import net.minecraft.core.player.inventory.slot.Slot;
import org.jetbrains.annotations.Nullable;

public class SlotOffhand extends Slot {
    private static final String OUTLINE_ICON = "better_with_hands:item/offhand_slot_outline";

    private final ContainerInventory inventory;

    public SlotOffhand(ContainerInventory inventory, int x, int y) {
        super(inventory, 0, x, y);
        this.inventory = inventory;
    }

    @Nullable
    public static SlotOffhand find(MenuAbstract menu) {
        for (Slot slot : menu.slots) {
            if (slot instanceof SlotOffhand offhandSlot) {
                return offhandSlot;
            }
        }
        return null;
    }

    @Override
    public String getItemIcon() {
        return OUTLINE_ICON;
    }

    @Override
    public boolean isLocked() {
        return false;
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
