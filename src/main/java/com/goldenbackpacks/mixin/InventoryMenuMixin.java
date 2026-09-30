package com.goldenbackpacks.mixin;

import com.goldenbackpacks.GoldenBackpacks;
import com.goldenbackpacks.compat.AccessoriesCompat;
import com.goldenbackpacks.compat.CuriosCompat;
import com.goldenbackpacks.item.BackpackItem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(InventoryMenu.class)
public abstract class InventoryMenuMixin {

    private static final ThreadLocal<Boolean> GOLDEN_BACKPACKS$QUICK_MOVE_GUARD = ThreadLocal.withInitial(() -> false);

    @Inject(method = "quickMoveStack", at = @At("HEAD"), cancellable = true)
    private void goldenbackpacks$quickMoveStack(Player player, int index, CallbackInfoReturnable<ItemStack> cir) {
        if (player.level().isClientSide || GOLDEN_BACKPACKS$QUICK_MOVE_GUARD.get()) {
            return;
        }

        if ((!GoldenBackpacks.CURIOS_LOADED && !GoldenBackpacks.ACCESSORIES_LOADED) || !GoldenBackpacks.CONFIG.enableTrinketCompatibility) {
            return;
        }

        Slot slot = ((InventoryMenu) (Object) this).getSlot(index);
        if (slot == null || !slot.hasItem()) {
            return;
        }

        // Only handle quick-equip from the player's main inventory/hotbar.
        // This avoids re-entering trinket auto-equip logic from armor/crafting/accessory slots.
        if (slot.container != player.getInventory()) {
            return;
        }
        int containerSlot = slot.getContainerSlot();
        if (containerSlot < 0 || containerSlot >= 36) {
            return;
        }

        ItemStack stack = slot.getItem();
        if (!(stack.getItem() instanceof BackpackItem) && stack.getItem() != GoldenBackpacks.ENDER_POUCH.get()) {
            return;
        }

        GOLDEN_BACKPACKS$QUICK_MOVE_GUARD.set(true);
        try {
            ItemStack original = stack.copy();
            if (GoldenBackpacks.ACCESSORIES_LOADED && AccessoriesCompat.tryEquipBackpack(player, stack)) {
                if (stack.isEmpty()) {
                    slot.set(ItemStack.EMPTY);
                } else {
                    slot.setChanged();
                }
                slot.onTake(player, original);
                cir.setReturnValue(original);
                return;
            }

            if (GoldenBackpacks.CURIOS_LOADED && CuriosCompat.tryEquipBackpack(player, stack)) {
                if (stack.isEmpty()) {
                    slot.set(ItemStack.EMPTY);
                } else {
                    slot.setChanged();
                }
                slot.onTake(player, original);
                cir.setReturnValue(original);
            }
        } finally {
            GOLDEN_BACKPACKS$QUICK_MOVE_GUARD.set(false);
        }
    }
}
