package com.goldenbackpacks.compat;

import com.goldenbackpacks.GoldenBackpacks;
import com.goldenbackpacks.config.BackpackInfo;
import com.goldenbackpacks.item.BackpackItem;
import com.goldenbackpacks.item.component.BackpackComponent;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.PlayerEnderChestContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public final class BackpackedConversion {

    public static final ResourceLocation BACKPACKED_ITEM_ID = ResourceLocation.fromNamespaceAndPath("backpacked", "backpack");

    private BackpackedConversion() {
    }

    public static Optional<BackpackTarget> resolveTarget(String tierName) {
        if (GoldenBackpacks.CONFIG == null) {
            return Optional.empty();
        }

        List<BackpackInfo> infos = GoldenBackpacks.CONFIG.backpacks;
        for (int i = 0; i < infos.size(); i++) {
            BackpackInfo info = infos.get(i);
            if (info.getName().equals(tierName)) {
                if (i < GoldenBackpacks.BACKPACKS.size()) {
                    return Optional.of(new BackpackTarget(info, GoldenBackpacks.BACKPACKS.get(i)));
                }
                break;
            }
        }

        return Optional.empty();
    }

    public static int convertPlayerInventories(ServerPlayer player, BackpackItem replacement) {
        Inventory inventory = player.getInventory();
        int converted = 0;
        converted += convertStacks(inventory.items, replacement);
        converted += convertStacks(inventory.armor, replacement);
        converted += convertStacks(inventory.offhand, replacement);
        converted += convertEnderChest(player.getEnderChestInventory(), replacement);

        if (converted > 0) {
            inventory.setChanged();
        }

        if (GoldenBackpacks.CURIOS_LOADED && GoldenBackpacks.CONFIG.enableTrinketCompatibility) {
            converted += CuriosCompat.replaceMatchingStacks(player, BackpackedConversion::isBackpackedStack,
                    stack -> copyStackAsGoldenBackpacks(stack, replacement));
        }
        if (GoldenBackpacks.ACCESSORIES_LOADED && GoldenBackpacks.CONFIG.enableTrinketCompatibility) {
            converted += AccessoriesCompat.replaceMatchingStacks(player, BackpackedConversion::isBackpackedStack,
                    stack -> copyStackAsGoldenBackpacks(stack, replacement));
        }

        return converted;
    }

    private static int convertStacks(List<ItemStack> stacks, BackpackItem replacement) {
        int converted = 0;
        for (int i = 0; i < stacks.size(); i++) {
            ItemStack stack = stacks.get(i);
            if (isBackpackedStack(stack)) {
                ItemStack convertedStack = copyStackAsGoldenBackpacks(stack, replacement);
                if (!convertedStack.isEmpty()) {
                    stacks.set(i, convertedStack);
                    converted++;
                }
            }
        }
        return converted;
    }

    private static int convertEnderChest(PlayerEnderChestContainer inventory, BackpackItem replacement) {
        int converted = 0;
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (isBackpackedStack(stack)) {
                ItemStack convertedStack = copyStackAsGoldenBackpacks(stack, replacement);
                if (!convertedStack.isEmpty()) {
                    inventory.setItem(slot, convertedStack);
                    converted++;
                }
            }
        }

        if (converted > 0) {
            inventory.setChanged();
        }

        return converted;
    }

    public static boolean isBackpackedStack(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }

        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return id != null && id.equals(BACKPACKED_ITEM_ID);
    }

    private static ItemStack copyStackAsGoldenBackpacks(ItemStack stack, Item replacement) {
        ResourceLocation targetId = BuiltInRegistries.ITEM.getKey(replacement);
        if (targetId == null) {
            return ItemStack.EMPTY;
        }

        ItemStack result = new ItemStack(replacement);

        ItemContainerContents backpackedContents = stack.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY);

        List<ItemStack> items = new ArrayList<>();
        backpackedContents.stream().forEach(items::add);

        if (!items.isEmpty()) {
            result.set(GoldenBackpacks.BACKPACK_COMPONENT.get(), new BackpackComponent(items));
        }

        return result;
    }

    public record BackpackTarget(BackpackInfo info, Supplier<BackpackItem> itemSupplier) {
    }
}
