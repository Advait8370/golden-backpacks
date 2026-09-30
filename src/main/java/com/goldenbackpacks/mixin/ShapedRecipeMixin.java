package com.goldenbackpacks.mixin;

import com.goldenbackpacks.GoldenBackpacks;
import com.goldenbackpacks.item.BackpackItem;
import com.goldenbackpacks.item.component.BackpackAugmentsComponent;
import com.goldenbackpacks.item.component.BackpackComponent;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.ShapedRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(ShapedRecipe.class)
public abstract class ShapedRecipeMixin {

    @Shadow
    public abstract ItemStack getResultItem(HolderLookup.Provider registries);

    @Inject(method = "assemble", at = @At("HEAD"), cancellable = true)
    private void onCraft(CraftingInput craftingInput, HolderLookup.Provider registries, CallbackInfoReturnable<ItemStack> cir) {
        if (craftingInput.size() <= 4) {
            return;
        }

        ItemStack centerSlot = craftingInput.getItem(4);
        if (!(centerSlot.getItem() instanceof BackpackItem) || GoldenBackpacks.isBackpackEmpty(centerSlot)) {
            return;
        }

        ItemStack newBackpack = this.getResultItem(registries).copy();
        if (newBackpack.getItem() instanceof BackpackItem backpackItem) {
            int size = backpackItem.getTier().getRowWidth() * backpackItem.getTier().getNumberOfRows();
            List<ItemStack> contents = GoldenBackpacks.getBackpackContents(centerSlot);
            List<ItemStack> newContents = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                if (i < contents.size()) {
                    newContents.add(contents.get(i).copy());
                } else {
                    newContents.add(ItemStack.EMPTY);
                }
            }
            newBackpack.set(GoldenBackpacks.BACKPACK_COMPONENT.get(), new BackpackComponent(newContents));
            BackpackAugmentsComponent augments = centerSlot.get(GoldenBackpacks.BACKPACK_AUGMENTS.get());
            if (augments != null) {
                newBackpack.set(GoldenBackpacks.BACKPACK_AUGMENTS.get(), augments);
            }
            cir.setReturnValue(newBackpack);
        }
    }
}
