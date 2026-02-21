package com.piti.ptm.block.entity;

import com.piti.ptm.network.EnergyNetwork;
import com.piti.ptm.network.NetworkManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.ForgeCapabilities;

import java.util.LinkedList;
import java.util.Queue;

public class CableBlockEntity extends BlockEntity {
    private EnergyNetwork network;

    public enum CableMode { NEUTRAL, IMPORT, EXPORT }
    private CableMode mode = CableMode.NEUTRAL;

    public CableBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CABLE_BE.get(), pos, state);
    }

    public void setMode(CableMode mode) {
        this.mode = mode;
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            updateNetwork();
        }
    }

    public CableMode getMode() {
        return mode;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level != null && !level.isClientSide) {
            NetworkManager.addCable(this);
            updateNetwork();
        }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (level != null && !level.isClientSide) {
            NetworkManager.removeCable(this);
            if (network != null) {
                NetworkManager.invalidateNetwork(network);
            }
        }
    }

    public void setNetwork(EnergyNetwork network) { this.network = network; }
    public EnergyNetwork getNetwork() { return network; }

    public void updateNetwork() {
        if (level == null || level.isClientSide) return;
        if (network != null) NetworkManager.invalidateNetwork(network);

        EnergyNetwork newNet = new EnergyNetwork(level);
        floodFill(newNet, worldPosition);
        NetworkManager.registerNetwork(newNet);
    }

    private void floodFill(EnergyNetwork net, BlockPos start) {
        Queue<BlockPos> queue = new LinkedList<>();
        queue.add(start);

        while (!queue.isEmpty()) {
            BlockPos current = queue.poll();
            if (net.getCables().contains(current)) continue;

            net.addCable(current);
            BlockEntity be = level.getBlockEntity(current);
            if (be instanceof CableBlockEntity cable) {
                cable.setNetwork(net);
                cable.scanNeighbors();
                for (Direction dir : Direction.values()) {
                    BlockPos neighborPos = current.relative(dir);
                    if (level.getBlockEntity(neighborPos) instanceof CableBlockEntity) {
                        queue.add(neighborPos);
                    }
                }
            }
        }
    }

    public void scanNeighbors() {
        if (network == null) return;
        for (Direction dir : Direction.values()) {
            BlockPos pos = worldPosition.relative(dir);
            BlockEntity be = level.getBlockEntity(pos);

            if (be != null && !(be instanceof CableBlockEntity)) {
                Direction machineSide = dir.getOpposite();
                be.getCapability(ForgeCapabilities.ENERGY, machineSide).ifPresent(cap -> {
                    if (mode == CableMode.NEUTRAL || mode == CableMode.IMPORT) {
                        if (cap.canExtract()) {
                            network.addProvider(pos, machineSide);
                        }
                    }

                    if (mode == CableMode.NEUTRAL || mode == CableMode.EXPORT) {
                        if (cap.canReceive()) {
                            network.addReceiver(pos, machineSide);
                        }
                    }
                });
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag nbt) {
        super.saveAdditional(nbt);
        nbt.putString("Mode", mode.name());
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        if (nbt.contains("Mode")) {
            mode = CableMode.valueOf(nbt.getString("Mode"));
        }
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        tag.putString("Mode", mode.name());
        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}