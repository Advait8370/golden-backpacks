package com.goldenbackpacks.network;

import com.goldenbackpacks.GoldenBackpacks;
import com.goldenbackpacks.compat.AccessoriesCompat;
import com.goldenbackpacks.compat.CuriosCompat;
import com.goldenbackpacks.item.BackpackItem;
import com.goldenbackpacks.item.EnderBackpackItem;
import com.goldenbackpacks.item.component.BackpackAugmentsComponent;
import com.goldenbackpacks.ui.BackpackScreenHandler;
import net.minecraft.stats.Stats;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.PlayerEnderChestContainer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;

public final class ServerNetworking {

    private ServerNetworking() {
    }

    public static void registerPayloadHandlers(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToServer(
                OpenBackpackPayload.TYPE,
                OpenBackpackPayload.STREAM_CODEC,
                ServerNetworking::handleOpenBackpack);
        event.registrar("1").playToServer(
                UpdateBackpackAugmentsPayload.TYPE,
                UpdateBackpackAugmentsPayload.STREAM_CODEC,
                ServerNetworking::handleUpdateBackpackAugments);
    }

    public static void sendOpenBackpack() {
        PacketDistributor.sendToServer(new OpenBackpackPayload());
    }

    public static void sendUpdateBackpackAugments(BackpackAugmentsComponent augments) {
        PacketDistributor.sendToServer(new UpdateBackpackAugmentsPayload(augments));
    }

    private static void handleOpenBackpack(OpenBackpackPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            if (player == null) {
                return;
            }

            if (GoldenBackpacks.ACCESSORIES_LOADED && GoldenBackpacks.CONFIG.enableTrinketCompatibility) {
                ItemStack accessoryBackpack = AccessoriesCompat.findFirstEquippedBackpack(player);
                if (!accessoryBackpack.isEmpty()) {
                    BackpackItem.openScreen(player, accessoryBackpack);
                    return;
                }
            }

            if (GoldenBackpacks.CURIOS_LOADED && GoldenBackpacks.CONFIG.enableTrinketCompatibility) {
                ItemStack curioBackpack = CuriosCompat.findFirstEquippedBackpack(player);
                if (!curioBackpack.isEmpty()) {
                    BackpackItem.openScreen(player, curioBackpack);
                    return;
                }
            }

            Inventory inventory = player.getInventory();
            ItemStack firstBackpackItemStack = ItemStack.EMPTY;

            if (!GoldenBackpacks.CONFIG.requireArmorTrinketToOpen) {
                firstBackpackItemStack = findFirstBackpack(inventory.offhand);
                if (firstBackpackItemStack.isEmpty()) {
                    firstBackpackItemStack = findFirstBackpack(inventory.items);
                }
            }

            if (firstBackpackItemStack.isEmpty()) {
                firstBackpackItemStack = findFirstBackpack(inventory.armor);
            }

            if (!firstBackpackItemStack.isEmpty()) {
                BackpackItem.openScreen(player, firstBackpackItemStack);
                return;
            }

            if (findFirstEnderPouch(inventory) != ItemStack.EMPTY) {
                openEnderPouch(player);
            }
        });
    }

    private static void handleUpdateBackpackAugments(UpdateBackpackAugmentsPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            if (player == null) {
                return;
            }
            if (player.containerMenu instanceof BackpackScreenHandler handler) {
                ItemStack stack = handler.getBackpackStack();
                if (stack.getItem() instanceof BackpackItem) {
                    stack.set(GoldenBackpacks.BACKPACK_AUGMENTS.get(), payload.augments());
                }
            }
        });
    }

    private static ItemStack findFirstBackpack(List<ItemStack> items) {
        for (ItemStack stack : items) {
            if (stack.getItem() instanceof BackpackItem) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    private static ItemStack findFirstEnderPouch(Inventory inventory) {
        for (ItemStack stack : inventory.items) {
            if (stack.getItem() == GoldenBackpacks.ENDER_POUCH.get()) {
                return stack;
            }
        }
        for (ItemStack stack : inventory.offhand) {
            if (stack.getItem() == GoldenBackpacks.ENDER_POUCH.get()) {
                return stack;
            }
        }
        for (ItemStack stack : inventory.armor) {
            if (stack.getItem() == GoldenBackpacks.ENDER_POUCH.get()) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    private static void openEnderPouch(Player player) {
        if (player.level().isClientSide) {
            return;
        }
        PlayerEnderChestContainer enderChestInventory = player.getEnderChestInventory();
        if (enderChestInventory == null) {
            return;
        }
        player.openMenu(new SimpleMenuProvider(
                (id, playerInventory, playerEntity) -> ChestMenu.threeRows(id, playerInventory, enderChestInventory),
                EnderBackpackItem.CONTAINER_NAME));
        player.awardStat(Stats.OPEN_ENDERCHEST);
    }
}
