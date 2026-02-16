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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class CableBlockEntity extends BlockEntity {
    private EnergyNetwork network;

    public enum CableMode {
        BOTH,
        EXTRACT,
        INSERT
    }

    private final Map<Direction, CableMode> sideModes = new HashMap<>();

    public CableBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CABLE_BE.get(), pos, state);
        for (Direction dir : Direction.values()) {
            sideModes.put(dir, CableMode.BOTH);
        }
    }

    public void toggleSideMode(Direction side) {
        CableMode current = sideModes.getOrDefault(side, CableMode.BOTH);
        CableMode next = switch (current) {
            case BOTH -> CableMode.EXTRACT;
            case EXTRACT -> CableMode.INSERT;
            case INSERT -> CableMode.BOTH;
        };
        sideModes.put(side, next);
        setChanged();
        if (level != null && !level.isClientSide) {
            updateNetwork();
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public CableMode getSideMode(Direction dir) {
        return sideModes.getOrDefault(dir, CableMode.BOTH);
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
        if (this.network != null) {
            NetworkManager.invalidateNetwork(this.network);
        }
        EnergyNetwork net = new EnergyNetwork(level);
        NetworkManager.registerNetwork(net);
        floodFill(net, worldPosition);
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
                CableMode mode = getSideMode(dir);
                be.getCapability(ForgeCapabilities.ENERGY, machineSide).ifPresent(cap -> {
                    if (cap.canExtract() && (mode == CableMode.BOTH || mode == CableMode.EXTRACT)) {
                        network.addProvider(pos, machineSide);
                    }
                    if (cap.canReceive() && (mode == CableMode.BOTH || mode == CableMode.INSERT)) {
                        network.addReceiver(pos, machineSide);
                    }
                });
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag nbt) {
        super.saveAdditional(nbt);
        for (Direction dir : Direction.values()) {
            nbt.putString("Mode_" + dir.getName(), sideModes.get(dir).name());
        }
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        for (Direction dir : Direction.values()) {
            String key = "Mode_" + dir.getName();
            if (nbt.contains(key)) {
                sideModes.put(dir, CableMode.valueOf(nbt.getString(key)));
            }
        }
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        saveAdditional(tag);
        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}