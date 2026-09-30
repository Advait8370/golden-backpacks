package com.goldenbackpacks.mixin;

import com.google.gson.JsonElement;
import com.goldenbackpacks.GoldenBackpacks;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.crafting.RecipeManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.Set;

@Mixin(RecipeManager.class)
public class RecipeManagerMixin {

    private static final Set<ResourceLocation> GOLDEN_BACKPACKS$BACKPACKED_RECIPES = Set.of(
            ResourceLocation.fromNamespaceAndPath("backpacked", "backpack"),
            ResourceLocation.fromNamespaceAndPath("backpacked", "acacia_backpack_shelf"),
            ResourceLocation.fromNamespaceAndPath("backpacked", "birch_backpack_shelf"),
            ResourceLocation.fromNamespaceAndPath("backpacked", "cherry_backpack_shelf"),
            ResourceLocation.fromNamespaceAndPath("backpacked", "crimson_backpack_shelf"),
            ResourceLocation.fromNamespaceAndPath("backpacked", "dark_oak_backpack_shelf"),
            ResourceLocation.fromNamespaceAndPath("backpacked", "jungle_backpack_shelf"),
            ResourceLocation.fromNamespaceAndPath("backpacked", "oak_backpack_shelf"),
            ResourceLocation.fromNamespaceAndPath("backpacked", "spruce_backpack_shelf"),
            ResourceLocation.fromNamespaceAndPath("backpacked", "warped_backpack_shelf")
    );

    @Inject(method = "apply", at = @At("HEAD"))
    private void goldenbackpacks$blockBackpackedRecipes(Map<ResourceLocation, JsonElement> map, ResourceManager resourceManager,
                                              ProfilerFiller profiler, CallbackInfo ci) {
        if (map.isEmpty() || GoldenBackpacks.CONFIG == null || !GoldenBackpacks.CONFIG.importBackpackedItems) {
            return;
        }

        int removed = 0;
        for (ResourceLocation id : GOLDEN_BACKPACKS$BACKPACKED_RECIPES) {
            if (map.remove(id) != null) {
                removed++;
            }
        }

        if (removed > 0) {
            GoldenBackpacks.LOGGER.debug("Blocked {} Backpacked recipe(s) because importBackpackedItems is enabled.", removed);
        }
    }
}
