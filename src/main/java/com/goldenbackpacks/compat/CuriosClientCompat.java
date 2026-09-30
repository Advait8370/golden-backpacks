package com.goldenbackpacks.compat;

import com.goldenbackpacks.GoldenBackpacks;
import com.goldenbackpacks.client.CuriosBackpackRenderer;
import com.goldenbackpacks.item.BackpackItem;
import top.theillusivec4.curios.api.client.CuriosRendererRegistry;

public final class CuriosClientCompat {

    private CuriosClientCompat() {
    }

    public static void registerRenderers() {
        if (!GoldenBackpacks.CONFIG.trinketRendering) {
            return;
        }

        for (var backpack : GoldenBackpacks.BACKPACKS) {
            BackpackItem item = backpack.get();
            CuriosRendererRegistry.register(item, CuriosBackpackRenderer::new);
        }
    }
}
