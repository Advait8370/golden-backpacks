package com.goldenbackpacks;

import com.goldenbackpacks.client.BackpackFeature;
import com.goldenbackpacks.client.GoldenBackpacksKeybinds;
import com.goldenbackpacks.compat.AccessoriesClientCompat;
import com.goldenbackpacks.compat.CuriosClientCompat;
import com.goldenbackpacks.item.BackpackItem;
import com.goldenbackpacks.item.DyeableBackpackItem;
import com.goldenbackpacks.ui.BackpackHandledScreen;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.world.item.component.DyedItemColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = GoldenBackpacks.MOD_ID, dist = Dist.CLIENT)
public class GoldenBackpacksClient {

    public GoldenBackpacksClient(IEventBus eventBus, ModContainer modContainer) {
        eventBus.addListener(this::setupClient);
        eventBus.addListener(this::registerMenuScreens);
        eventBus.addListener(this::registerKeyMappings);
        eventBus.addListener(this::registerItemColors);
        eventBus.addListener(this::addLayers);
    }

    private void setupClient(final FMLClientSetupEvent event) {
        NeoForge.EVENT_BUS.addListener(GoldenBackpacksKeybinds::onClientTick);

        if (GoldenBackpacks.CURIOS_LOADED && GoldenBackpacks.CONFIG.enableTrinketCompatibility) {
            event.enqueueWork(CuriosClientCompat::registerRenderers);
        }
        if (GoldenBackpacks.ACCESSORIES_LOADED && GoldenBackpacks.CONFIG.enableTrinketCompatibility) {
            event.enqueueWork(AccessoriesClientCompat::registerRenderers);
        }
    }

    private void registerMenuScreens(final RegisterMenuScreensEvent event) {
        event.register(GoldenBackpacks.CONTAINER_TYPE.get(), BackpackHandledScreen::new);
    }

    private void registerKeyMappings(final RegisterKeyMappingsEvent event) {
        GoldenBackpacksKeybinds.register(event);
    }

    private void registerItemColors(final RegisterColorHandlersEvent.Item event) {
        for (var backpackEntry : GoldenBackpacks.BACKPACKS) {
            BackpackItem backpack = backpackEntry.get();
            if (backpack instanceof DyeableBackpackItem) {
                event.register((stack, tintIndex) -> tintIndex > 0
                        ? -1
                        : DyedItemColor.getOrDefault(stack, DyedItemColor.LEATHER_COLOR), backpack);
            }
        }
    }

    private void addLayers(final EntityRenderersEvent.AddLayers event) {
        for (PlayerSkin.Model skin : event.getSkins()) {
            if (event.getSkin(skin) instanceof PlayerRenderer renderer) {
                renderer.addLayer(new BackpackFeature(renderer));
            }
        }
    }
}
