package com.goldenbackpacks.augment;

import com.goldenbackpacks.GoldenBackpacks;
import com.goldenbackpacks.config.BackpackInfo;
import com.goldenbackpacks.item.BackpackItem;
import com.goldenbackpacks.item.component.BackpackAugmentsComponent;
import com.goldenbackpacks.item.component.BackpackComponent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.entity.Hopper;

import java.util.List;
import java.util.function.Predicate;

public class BackpackInventory extends SimpleContainer {

    private final ItemStack backpackStack;
    private final BackpackInfo tier;

    public BackpackInventory(ItemStack backpackStack, BackpackInfo tier) {
        super(Math.max(0, tier.getRowWidth() * tier.getNumberOfRows()));
        this.backpackStack = backpackStack;
        this.tier = tier;

        BackpackComponent component = GoldenBackpacks.getOrCreateComponent(backpackStack, tier);
        List<ItemStack> stacks = component != null ? component.stacks() : List.of();
        for (int i = 0; i < getContainerSize(); i++) {
            setItem(i, i < stacks.size() ? stacks.get(i) : ItemStack.EMPTY);
        }
    }

    public ItemStack getBackpackStack() {
        return backpackStack;
    }

    @Override
    public void setChanged() {
        backpackStack.set(GoldenBackpacks.BACKPACK_COMPONENT.get(), BackpackComponent.fromContainer(this));
        super.setChanged();
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return super.canPlaceItem(slot, stack) && isAllowedItem(stack);
    }

    @Override
    public boolean canTakeItem(Container container, int slot, ItemStack stack) {
        if (container instanceof Hopper) {
            BackpackAugmentsComponent augments = GoldenBackpacks.getOrCreateAugments(backpackStack, tier);
            if (BackpackAugments.isUnlocked(tier, BackpackAugmentType.HOPPER_BRIDGE)
                    && augments.hopperBridge().enabled()) {
                BackpackAugmentsComponent.HopperBridgeSettings settings = augments.hopperBridge();
                if (!settings.extract()) {
                    return false;
                }
                if (settings.filterMode().checkExtract() && isFilteredOut(stack, settings.filters())) {
                    return false;
                }
            }
        }
        return super.canTakeItem(container, slot, stack);
    }

    public ItemStack findFirst(Predicate<ItemStack> predicate) {
        for (int i = 0; i < getContainerSize(); i++) {
            ItemStack stack = getItem(i);
            if (!stack.isEmpty() && predicate.test(stack)) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    public boolean isAllowedItem(ItemStack stack) {
        if (stack.getItem() instanceof BackpackItem) {
            return false;
        }
        if (GoldenBackpacks.CONFIG.unstackablesOnly && stack.getMaxStackSize() > 1) {
            return false;
        }
        if (GoldenBackpacks.CONFIG.disableShulkers && stack.getItem() instanceof BlockItem blockItem) {
            return !(blockItem.getBlock() instanceof ShulkerBoxBlock);
        }
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (id != null && GoldenBackpacks.CONFIG.blacklist != null && GoldenBackpacks.CONFIG.blacklist.contains(id.toString())) {
            return false;
        }
        return true;
    }

    private boolean isFilteredOut(ItemStack stack, List<ResourceLocation> filters) {
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return id == null || !filters.contains(id);
    }
}
