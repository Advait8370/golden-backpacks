package com.goldenbackpacks.augment;

import com.goldenbackpacks.GoldenBackpacks;
import com.goldenbackpacks.compat.AccessoriesCompat;
import com.goldenbackpacks.compat.CuriosCompat;
import com.goldenbackpacks.item.BackpackItem;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public final class BackpackAugmentHelper {

    private BackpackAugmentHelper() {
    }

    public static List<ItemStack> getBackpackStacks(Player player) {
        List<ItemStack> stacks = new ArrayList<>();
        Inventory inventory = player.getInventory();
        collectBackpacks(inventory.items, stacks);
        collectBackpacks(inventory.armor, stacks);
        collectBackpacks(inventory.offhand, stacks);

        if (GoldenBackpacks.ACCESSORIES_LOADED && GoldenBackpacks.CONFIG.enableTrinketCompatibility) {
            stacks.addAll(AccessoriesCompat.getEquippedBackpacks(player));
        }

        if (GoldenBackpacks.CURIOS_LOADED && GoldenBackpacks.CONFIG.enableTrinketCompatibility) {
            stacks.addAll(CuriosCompat.getEquippedBackpacks(player));
        }

        return stacks;
    }

    private static void collectBackpacks(List<ItemStack> items, List<ItemStack> target) {
        for (ItemStack stack : items) {
            if (!stack.isEmpty() && stack.getItem() instanceof BackpackItem) {
                target.add(stack);
            }
        }
    }
}
