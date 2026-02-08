package com.piti.ptm.block.entity;

import com.piti.ptm.block.custom.PipeBlock;
import com.piti.ptm.block.entity.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class PipeBlockEntity extends BlockEntity {

    private String filterFluidID = "";

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
            BlockState state = getBlockState();
            BlockState newState = ((PipeBlock) state.getBlock()).updateConnections(level, worldPosition, state);
            if (newState != state) {
                level.setBlock(worldPosition, newState, 3);
            }
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

        if (level != null && level.isClientSide) {
            BlockState state = getBlockState();
            level.sendBlockUpdated(worldPosition, state, state, 3);
        }
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        tag.putString("FluidID", filterFluidID);
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
        load(tag);
    }
}