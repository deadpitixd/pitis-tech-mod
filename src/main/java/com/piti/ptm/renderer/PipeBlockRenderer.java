package com.piti.ptm.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.piti.ptm.PitisTech;
import com.piti.ptm.block.custom.PipeBlock;
import com.piti.ptm.block.entity.PipeBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

public class PipeBlockRenderer implements BlockEntityRenderer<PipeBlockEntity> {
    private final BlockRenderDispatcher dispatcher;
    private static final ResourceLocation TOGGLE_MODEL_RL = ResourceLocation.fromNamespaceAndPath(PitisTech.MOD_ID, "block/pipe_toggle");

    public PipeBlockRenderer(BlockEntityRendererProvider.Context context) {
        this.dispatcher = context.getBlockRenderDispatcher();
    }

    @Override
    public void render(PipeBlockEntity entity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        PipeBlockEntity.PipeMode mode = entity.getMode();
        if (mode == PipeBlockEntity.PipeMode.NEUTRAL || entity.getLevel() == null) return;

        BlockState state = entity.getBlockState();
        BakedModel toggleModel = dispatcher.getBlockModelShaper().getModelManager().getModel(TOGGLE_MODEL_RL);

        for (Direction direction : Direction.values()) {
            if (shouldRenderToggleOnFace(entity, state, direction)) {
                poseStack.pushPose();

                // 1. Center in block
                poseStack.translate(0.5, 0.5, 0.5);

                applyFaceRotation(poseStack, direction);

                poseStack.translate(0.0625 * 9 - (0.0625 / 2), 0, 0);

                if (mode == PipeBlockEntity.PipeMode.IMPORT) {
                    poseStack.mulPose(Axis.YP.rotationDegrees(180f));
                }

                poseStack.translate(-0.5, -0.5, -0.5);

                dispatcher.getModelRenderer().renderModel(
                        poseStack.last(),
                        bufferSource.getBuffer(RenderType.cutout()),
                        state,
                        toggleModel,
                        1.0f, 1.0f, 1.0f,
                        packedLight,
                        packedOverlay
                );

                poseStack.popPose();
            }
        }
    }

    private boolean shouldRenderToggleOnFace(PipeBlockEntity entity, BlockState state, Direction dir) {
        boolean isConnected = switch (dir) {
            case NORTH -> state.getValue(PipeBlock.NORTH);
            case SOUTH -> state.getValue(PipeBlock.SOUTH);
            case EAST -> state.getValue(PipeBlock.EAST);
            case WEST -> state.getValue(PipeBlock.WEST);
            case UP -> state.getValue(PipeBlock.UP);
            case DOWN -> state.getValue(PipeBlock.DOWN);
        };
        if (!isConnected) return false;
        return !(entity.getLevel().getBlockEntity(entity.getBlockPos().relative(dir)) instanceof PipeBlockEntity);
    }

    private void applyFaceRotation(PoseStack poseStack, Direction direction) {
        switch (direction) {
            case EAST -> { }
            case WEST -> poseStack.mulPose(Axis.YP.rotationDegrees(180f));
            case NORTH -> poseStack.mulPose(Axis.YP.rotationDegrees(90f));
            case SOUTH -> poseStack.mulPose(Axis.YP.rotationDegrees(-90f));
            case UP -> poseStack.mulPose(Axis.ZP.rotationDegrees(90f));
            case DOWN -> poseStack.mulPose(Axis.ZP.rotationDegrees(-90f));
        }
    }
}