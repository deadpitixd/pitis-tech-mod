package com.piti.ptm.block.entity;

import com.piti.ptm.block.custom.PipeBlock;
import com.piti.ptm.fluid.BaseFluidType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

public class PipeBlockEntity extends BlockEntity {

    public boolean toggleActive = false;
    private String filterFluidID = "none";
    public int color = 0x80333333;

    public enum PipeMode { NEUTRAL, IMPORT, EXPORT }
    private PipeMode mode = PipeMode.NEUTRAL;

    public PipeBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.PIPE.get(), pos, state);
    }

    public String getFilterFluidID() {
        return filterFluidID;
    }

    public boolean showToggle() {
        return mode != PipeMode.NEUTRAL;
    }

    public PipeMode getMode() { return mode; }

    public void setMode(PipeMode newMode) {
        if (newMode == null) newMode = PipeMode.NEUTRAL;
        if (newMode == mode) return;
        this.mode = newMode;
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public void setFilterFluidID(String id) {
        if (id == null) id = "";
        if (id.equals(this.filterFluidID)) return;
        this.filterFluidID = id;
        this.color = computeTintFromFluidId(id);
        this.setChanged();

        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);

            if (!level.isClientSide) {
                refreshSelfAndNeighbors();
            }
        }
    }

    private int computeTintFromFluidId(String fluidId) {
        if (fluidId == null || fluidId.isEmpty()) return 0x80333333;
        try {
            Fluid fluid = ForgeRegistries.FLUIDS.getValue(ResourceLocation.parse(fluidId));
            if (fluid == null || fluid == Fluids.EMPTY) return 0x80333333;
            if (fluid.isSame(Fluids.LAVA)) return 0xFFFF4500;
            int c = IClientFluidTypeExtensions.of(fluid).getTintColor();
            if (c == -1 || c == 0xFFFFFFFF) {
                if (fluid.getFluidType() instanceof BaseFluidType baseType) return baseType.getTintColor();
            }
            return c;
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
            if (!newState.equals(state)) level.setBlock(worldPosition, newState, 3);
        }
        for (Direction d : Direction.values()) {
            BlockPos p = worldPosition.relative(d);
            BlockState s = level.getBlockState(p);
            if (s.getBlock() instanceof PipeBlock pipe) {
                BlockState ns = pipe.updateConnections(level, p, s);
                if (ns != s) level.setBlock(p, ns, 3);
            }
        }
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    @Override
    protected void saveAdditional(CompoundTag nbt) {
        super.saveAdditional(nbt);
        nbt.putString("FluidID", filterFluidID);
        nbt.putInt("Color", color);
        nbt.putString("Mode", mode.name());
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        filterFluidID = nbt.getString("FluidID");
        color = nbt.contains("Color") ? nbt.getInt("Color") : computeTintFromFluidId(filterFluidID);
        try {
            mode = PipeMode.valueOf(nbt.getString("Mode"));
        } catch (Exception e) {
            mode = PipeMode.NEUTRAL;
        }
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        tag.putString("FluidID", filterFluidID);
        tag.putInt("Color", color);
        tag.putString("Mode", mode.name());
        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public FluidStack getFilterFluidStack() {
        if (filterFluidID == null || filterFluidID.isEmpty()) return FluidStack.EMPTY;
        Fluid fluid = ForgeRegistries.FLUIDS.getValue(ResourceLocation.parse(filterFluidID));
        if (fluid == null || fluid == Fluids.EMPTY) return FluidStack.EMPTY;
        return new FluidStack(fluid, 1);
    }

    @Override
    public void onDataPacket(net.minecraft.network.Connection net, ClientboundBlockEntityDataPacket pkt) {
        handleUpdateTag(pkt.getTag());
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag);
        filterFluidID = tag.getString("FluidID");
        color = tag.contains("Color") ? tag.getInt("Color") : computeTintFromFluidId(filterFluidID);
        try {
            mode = PipeMode.valueOf(tag.getString("Mode"));
        } catch (Exception e) {
            mode = PipeMode.NEUTRAL;
        }
    }
}