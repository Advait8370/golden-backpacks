package com.goldenbackpacks.client;

import com.goldenbackpacks.GoldenBackpacks;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

/** A compact 3D pack that follows the player's torso while equipped. */
public class BackpackModel extends Model {
    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(GoldenBackpacks.id("backpack"), "main");
    public static final ResourceLocation TEXTURE =
            GoldenBackpacks.id("textures/entity/backpack_3d.png");

    private final ModelPart backpack;

    public BackpackModel(ModelPart root) {
        super(RenderType::entityCutoutNoCull);
        this.backpack = root.getChild("backpack");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        root.addOrReplaceChild("backpack", CubeListBuilder.create()
                        // Positive Z is the player's back in the humanoid model coordinate system.
                        .texOffs(0, 0).addBox(-4.5F, 1.0F, 2.0F, 9.0F, 10.0F, 3.0F),
                PartPose.ZERO);

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight,
                               int packedOverlay, int color) {
        backpack.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}
