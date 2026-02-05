package com.piti.ptm.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
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
        this.filterFluidID = id;
        setChanged();
        System.out.println((level.isClientSide ? "[CLIENT]" : "[SERVER]") +
                " setFilterFluidID: " + id + " at " + worldPosition);

        if (level != null && !level.isClientSide) {
            BlockState state = getBlockState();
            level.sendBlockUpdated(worldPosition, state, state, 3);
            System.out.println("[SERVER] sendBlockUpdated called at " + worldPosition);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag nbt) {
        nbt.putString("FluidID", filterFluidID);
        super.saveAdditional(nbt);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        this.filterFluidID = nbt.getString("FluidID");
        System.out.println((level != null && level.isClientSide ? "[CLIENT]" : "[SERVER]") +
                " load called, FluidID=" + filterFluidID + " at " + worldPosition);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(net.minecraft.network.Connection net, ClientboundBlockEntityDataPacket pkt) {
        super.onDataPacket(net, pkt);
        this.load(pkt.getTag());
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        this.load(tag);
    }
}