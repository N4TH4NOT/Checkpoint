package me.n4th4not.checkpoint.client.render.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Quaternion;
import me.n4th4not.checkpoint.Main;
import me.n4th4not.checkpoint.level.block.WaystoneBlock;
import me.n4th4not.checkpoint.level.block.WaystoneEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import org.jetbrains.annotations.NotNull;

public class WaystoneRenderer
    implements BlockEntityRenderer<WaystoneEntity> {

    public static final ResourceLocation GLYPHS = new ResourceLocation("waystone_overlays/waystone_active");
    private static final Material MATERIAL = new Material(Main.SHEET, GLYPHS);

    private final WaystoneModel model;

    public WaystoneRenderer(BlockEntityRendererProvider.Context context) {
        this.model = new WaystoneModel(context.bakeLayer(Main.WAYSTONE_MODEL));
    }

    @Override
    public void render(WaystoneEntity tile, float p_tick, @NotNull PoseStack stack, @NotNull MultiBufferSource buffer, int combinedLightIn, int combinedOverlayIn) {
        BlockState state = tile.getBlockState();
        if (state.getValue(WaystoneBlock.HALF) != DoubleBlockHalf.LOWER) return;

        float angle = state.getValue(WaystoneBlock.FACING).toYRot();
        stack.pushPose();
        stack.translate(0.5f, 0f, 0.5f);
        stack.mulPose(new Quaternion(0f, angle, 0f, true));
        stack.mulPose(new Quaternion(-180f, 0f, 0f, true));
        stack.scale(0.5f, 0.5f, 0.5f);
        if (tile.getState()) {
            stack.scale(1.05f, 1.05f, 1.05f);
            VertexConsumer vertex = MATERIAL.buffer(buffer, RenderType::entityCutout);
            model.renderToBuffer(stack, vertex, 0xf000f0, OverlayTexture.NO_OVERLAY, 1f, 1f, 1f, 1f);
        }
        stack.popPose();
    }
}

