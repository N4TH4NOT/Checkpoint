package me.n4th4not.checkpoint.client.render.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.RenderType;
import org.jetbrains.annotations.NotNull;

public class WaystoneModel
    extends Model {

    private final ModelPart root;

    public WaystoneModel(ModelPart root) {
        super(it -> RenderType.cutout());
        this.root = root;
    }

    public static LayerDefinition createLayer(CubeDeformation def) {
        MeshDefinition mesh = new MeshDefinition();
        mesh.getRoot().addOrReplaceChild("body", CubeListBuilder.create().texOffs(144, 0).addBox(-10f, -48f, -10f, 20, 28, 20, def), PartPose.ZERO);
        return LayerDefinition.create(mesh, 256, 256);
    }

    @Override
    public void renderToBuffer(@NotNull PoseStack matrixStack, @NotNull VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        this.root.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
