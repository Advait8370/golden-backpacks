package com.goldenbackpacks.compat;

import com.goldenbackpacks.GoldenBackpacks;
import com.goldenbackpacks.config.BackpackInfo;
import com.goldenbackpacks.item.BackpackItem;
import com.goldenbackpacks.compat.BackpackedImportController;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.Optional;

public final class BackpackedMigrationManager {

    private static final Component SUCCESS_MESSAGE = Component.literal("[GoldenBackpacks] Migrated your Backpacked data.")
            .withStyle(ChatFormatting.GREEN);

    private static boolean warnedMissingTier;
    private static boolean warnedCapacity;
    private static boolean warnedImporterDisabled;

    private BackpackedMigrationManager() {
    }

    public static void bootstrapFromConfig() {
        BackpackedImportController.setOverride(null);
    }

    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!shouldAutoMigrate()) {
            return;
        }

        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        if (!BackpackedImportController.isImportEnabled()) {
            if (!warnedImporterDisabled) {
                warnedImporterDisabled = true;
                GoldenBackpacks.LOGGER.warn("Automatic Backpacked migration is enabled but importBackpackedItems is currently disabled. Run /goldenbackpacks import_backpacked enable or update your config.");
            }
            return;
        }

        Optional<BackpackedConversion.BackpackTarget> targetOptional =
                BackpackedConversion.resolveTarget(GoldenBackpacks.CONFIG.autoBackpackedTier);
        if (targetOptional.isEmpty()) {
            if (!warnedMissingTier) {
                warnedMissingTier = true;
                GoldenBackpacks.LOGGER.warn("Automatic Backpacked migration skipped because tier '{}' is not defined in goldenbackpacks.json.",
                        GoldenBackpacks.CONFIG.autoBackpackedTier);
            }
            return;
        }

        BackpackInfo tierInfo = targetOptional.get().info();
        BackpackItem targetItem = targetOptional.get().itemSupplier().get();

        int sourceSlots = Math.max(1, GoldenBackpacks.CONFIG.autoBackpackedColumns * GoldenBackpacks.CONFIG.autoBackpackedRows);
        int targetSlots = Math.max(1, tierInfo.getRowWidth() * tierInfo.getNumberOfRows());

        if (!GoldenBackpacks.CONFIG.autoBackpackedAllowSmaller && targetSlots < sourceSlots) {
            if (!warnedCapacity) {
                warnedCapacity = true;
                GoldenBackpacks.LOGGER.warn("Automatic Backpacked migration requires at least {} slots but tier '{}' only offers {}. Increase your target tier or enable autoBackpackedAllowSmaller.",
                        sourceSlots, tierInfo.getName(), targetSlots);
            }
            return;
        }

        int converted = BackpackedConversion.convertPlayerInventories(player, targetItem);
        if (converted > 0) {
            player.sendSystemMessage(SUCCESS_MESSAGE);
        }
    }

    private static boolean shouldAutoMigrate() {
        return GoldenBackpacks.CONFIG != null && GoldenBackpacks.CONFIG.importBackpackedItems;
    }
}
