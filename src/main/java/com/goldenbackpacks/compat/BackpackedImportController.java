package com.goldenbackpacks.compat;

import com.goldenbackpacks.GoldenBackpacks;

public final class BackpackedImportController {

    private static Boolean overrideValue = null;

    private BackpackedImportController() {
    }

    public static boolean isImportEnabled() {
        if (overrideValue != null) {
            return overrideValue;
        }

        return GoldenBackpacks.CONFIG != null && GoldenBackpacks.CONFIG.importBackpackedItems;
    }

    public static void setOverride(Boolean override) {
        overrideValue = override;
    }

    public static boolean isOverridden() {
        return overrideValue != null;
    }

    public static boolean getConfigDefault() {
        return GoldenBackpacks.CONFIG != null && GoldenBackpacks.CONFIG.importBackpackedItems;
    }
}
