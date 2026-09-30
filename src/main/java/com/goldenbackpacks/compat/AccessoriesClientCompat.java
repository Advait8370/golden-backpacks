package com.goldenbackpacks.compat;

import com.goldenbackpacks.GoldenBackpacks;
import com.goldenbackpacks.client.AccessoriesBackpackRenderer;
import io.wispforest.accessories.api.client.AccessoriesRendererRegistry;

public final class AccessoriesClientCompat {

    private AccessoriesClientCompat() {
    }

    public static void registerRenderers() {
        for (var backpack : GoldenBackpacks.BACKPACKS) {
            AccessoriesRendererRegistry.registerRenderer(backpack.get(), AccessoriesBackpackRenderer::new);
        }
        AccessoriesRendererRegistry.registerRenderer(GoldenBackpacks.ENDER_POUCH.get(), AccessoriesBackpackRenderer::new);
    }
}
