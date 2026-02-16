package com.piti.ptm.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.piti.ptm.PitisTech;
import com.piti.ptm.block.custom.CableBlock;
import com.piti.ptm.block.entity.CableBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

public class CableBlockRenderer implements BlockEntityRenderer<CableBlockEntity> {
    private final BlockRenderDispatcher dispatcher;
    private static final ResourceLocation TOGGLE_MODEL_RL = ResourceLocation.fromNamespaceAndPath(PitisTech.MOD_ID, "block/pipe_toggle");

    public CableBlockRenderer(BlockEntityRendererProvider.Context context) {
        this.dispatcher = context.getBlockRenderDispatcher();
    }

    @Override
    public void render(CableBlockEntity entity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        BlockState state = entity.getBlockState();
        BakedModel toggleModel = dispatcher.getBlockModelShaper().getModelManager().getModel(TOGGLE_MODEL_RL);

        for (Direction dir : Direction.values()) {
            CableBlockEntity.CableMode mode = entity.getSideMode(dir);
            if (mode == CableBlockEntity.CableMode.BOTH) continue;

            poseStack.pushPose();
            poseStack.translate(0.5, 0.5, 0.5);
            applyFaceRotation(poseStack, dir);
            poseStack.translate(-0.5, -0.5, -0.5);

            float r = mode == CableBlockEntity.CableMode.EXTRACT ? 0.2f : 1.0f;
            float g = mode == CableBlockEntity.CableMode.EXTRACT ? 0.2f : 0.6f;
            float b = mode == CableBlockEntity.CableMode.EXTRACT ? 1.0f : 0.2f;

            dispatcher.getModelRenderer().renderModel(
                    poseStack.last(),
                    bufferSource.getBuffer(RenderType.cutout()),
                    state,
                    toggleModel,
                    r, g, b,
                    packedLight,
                    packedOverlay
            );
            poseStack.popPose();
        }
    }

    private void applyFaceRotation(PoseStack poseStack, Direction direction) {
        switch (direction) {
            case WEST -> poseStack.mulPose(Axis.YP.rotationDegrees(180f));
            case NORTH -> poseStack.mulPose(Axis.YP.rotationDegrees(90f));
            case SOUTH -> poseStack.mulPose(Axis.YP.rotationDegrees(-90f));
            case UP -> poseStack.mulPose(Axis.ZP.rotationDegrees(90f));
            case DOWN -> poseStack.mulPose(Axis.ZP.rotationDegrees(-90f));
            default -> {}
        }
    }
}