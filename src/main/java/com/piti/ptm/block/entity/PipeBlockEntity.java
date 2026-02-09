package com.piti.ptm.block.entity;

import com.piti.ptm.block.custom.PipeBlock;
import com.piti.ptm.block.entity.ModBlockEntities;
import com.piti.ptm.fluid.BaseFluidType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.registries.ForgeRegistries;

public class PipeBlockEntity extends BlockEntity {

    private String filterFluidID = "";
    /* Color, i think that caching the color will be better,
    * As there will be no color update calls every tick.
    * */
    public int color = 0x00000;

    public PipeBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.PIPE.get(), pos, state);
    }

    public String getFilterFluidID() {
        return filterFluidID;
    }

    public void setFilterFluidID(String id) {
        if (id == null) id = "";
        if (id.equals(this.filterFluidID)) return;

        this.filterFluidID = id;
        setChanged();

        if (level != null && !level.isClientSide) {
            refreshSelfAndNeighbors();
        }
    }

    private int computeTintFromFluidId(String fluidId) {
        if (fluidId == null || fluidId.isEmpty()) {
            return 0x80333333; // default gray
        }

        try {
            Fluid fluid = ForgeRegistries.FLUIDS.getValue(ResourceLocation.parse(fluidId));
            if (fluid == null || fluid == Fluids.EMPTY) {
                return 0x80333333;
            }


            if (fluid.isSame(Fluids.LAVA)) {
                return 0xFFFF4500;
            }

            int color = IClientFluidTypeExtensions.of(fluid).getTintColor();

            if (color == -1 || color == 0xFFFFFFFF) {
                if (fluid.getFluidType() instanceof BaseFluidType baseType) {
                    return baseType.getTintColor();
                }
            }

            return color;

        } catch (Exception ignored) {
            return 0x80333333;
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();

        if (level != null && level.isClientSide) {
            this.color = computeTintFromFluidId(this.filterFluidID);
        }
    }

    private void refreshSelfAndNeighbors() {
        if (level == null) return;

        BlockState state = getBlockState();
        if (state.getBlock() instanceof PipeBlock pipe) {
            BlockState newState = pipe.updateConnections(level, worldPosition, state);
            if (newState != state) {
                level.setBlock(worldPosition, newState, 2);
            }
        }

        for (Direction d : Direction.values()) {
            BlockPos p = worldPosition.relative(d);
            BlockState s = level.getBlockState(p);

            if (s.getBlock() instanceof PipeBlock pipe) {
                BlockState ns = pipe.updateConnections(level, p, s);
                if (ns != s) {
                    level.setBlock(p, ns, 2);
                }
            }
        }

        BlockState s0 = level.getBlockState(worldPosition);
        level.sendBlockUpdated(worldPosition, s0, s0, 2);

        for (Direction d : Direction.values()) {
            BlockPos p = worldPosition.relative(d);
            BlockState sp = level.getBlockState(p);
            level.sendBlockUpdated(p, sp, sp, 2);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag nbt) {
        super.saveAdditional(nbt);
        nbt.putString("FluidID", filterFluidID);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        filterFluidID = nbt.getString("FluidID");
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        tag.putString("FluidID", filterFluidID);
        tag.putInt("Color", color);
        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(net.minecraft.network.Connection net, ClientboundBlockEntityDataPacket pkt) {
        super.onDataPacket(net, pkt);
        handleUpdateTag(pkt.getTag());
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag);

        this.filterFluidID = tag.getString("FluidID");

        if (level != null && level.isClientSide) {
            this.color = computeTintFromFluidId(this.filterFluidID);

            BlockState state = getBlockState();
            level.sendBlockUpdated(worldPosition, state, state, 2);
        }
    }
}