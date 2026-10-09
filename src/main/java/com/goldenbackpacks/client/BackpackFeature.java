package com.goldenbackpacks.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.goldenbackpacks.item.BackpackItem;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

public class BackpackFeature extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    private final BackpackModel backpackModel;

    public BackpackFeature(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> context,
                           BackpackModel backpackModel) {
        super(context);
        this.backpackModel = backpackModel;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, AbstractClientPlayer player,
                       float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        ItemStack chestSlot = player.getItemBySlot(EquipmentSlot.CHEST);
        if (!(chestSlot.getItem() instanceof BackpackItem)) {
            return;
        }

        poseStack.pushPose();
        getParentModel().body.translateAndRotate(poseStack);
        backpackModel.renderToBuffer(
                poseStack,
                bufferSource.getBuffer(backpackModel.renderType(BackpackModel.TEXTURE)),
                packedLight,
                OverlayTexture.NO_OVERLAY,
                -1);
        poseStack.popPose();
    }
}
