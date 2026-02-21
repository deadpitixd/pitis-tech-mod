package com.piti.ptm.network;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.energy.IEnergyStorage;

import java.util.HashSet;
import java.util.Set;

public class EnergyNetwork {
    private final Level level;
    private final Set<BlockPos> cables = new HashSet<>();
    private final Set<MachineLocation> providers = new HashSet<>();
    private final Set<MachineLocation> receivers = new HashSet<>();

    public record MachineLocation(BlockPos pos, Direction side) {}

    public EnergyNetwork(Level level) {
        this.level = level;
    }

    public int receiveEnergy(int maxReceive, boolean simulate) {
        int totalReceived = 0;
        int remaining = maxReceive;
        for (MachineLocation loc : receivers) {
            if (remaining <= 0) break;
            BlockEntity be = level.getBlockEntity(loc.pos);
            if (be != null) {
                IEnergyStorage storage = be.getCapability(ForgeCapabilities.ENERGY, loc.side).orElse(null);
                if (storage != null) {
                    int received = storage.receiveEnergy(remaining, simulate);
                    totalReceived += received;
                    remaining -= received;
                }
            }
        }
        return totalReceived;
    }

    public int extractEnergy(int maxExtract, boolean simulate) {
        int totalExtracted = 0;
        int remaining = maxExtract;
        for (MachineLocation loc : providers) {
            if (remaining <= 0) break;
            BlockEntity be = level.getBlockEntity(loc.pos);
            if (be != null) {
                IEnergyStorage storage = be.getCapability(ForgeCapabilities.ENERGY, loc.side).orElse(null);
                if (storage != null) {
                    int extracted = storage.extractEnergy(remaining, simulate);
                    totalExtracted += extracted;
                    remaining -= extracted;
                }
            }
        }
        return totalExtracted;
    }

    public void tick() {
        if (providers.isEmpty() || receivers.isEmpty()) return;

        long totalExtractable = 0;
        for (MachineLocation loc : providers) {
            BlockEntity be = level.getBlockEntity(loc.pos);
            if (be != null) {
                IEnergyStorage storage = be.getCapability(ForgeCapabilities.ENERGY, loc.side).orElse(null);
                if (storage != null && storage.canExtract()) {
                    totalExtractable += (long) storage.extractEnergy(Integer.MAX_VALUE, true);
                }
            }
        }

        if (totalExtractable <= 0) return;

        long totalDemand = 0;
        for (MachineLocation loc : receivers) {
            BlockEntity be = level.getBlockEntity(loc.pos);
            if (be != null) {
                IEnergyStorage storage = be.getCapability(ForgeCapabilities.ENERGY, loc.side).orElse(null);
                if (storage != null && storage.canReceive()) {
                    totalDemand += (long) storage.receiveEnergy(Integer.MAX_VALUE, true);
                }
            }
        }

        if (totalDemand <= 0) return;

        int toTransfer = (int) Math.min(totalExtractable, totalDemand);
        int extracted = 0;

        for (MachineLocation loc : providers) {
            if (extracted >= toTransfer) break;
            BlockEntity be = level.getBlockEntity(loc.pos);
            if (be != null) {
                IEnergyStorage storage = be.getCapability(ForgeCapabilities.ENERGY, loc.side).orElse(null);
                if (storage != null && storage.canExtract()) {
                    extracted += storage.extractEnergy(toTransfer - extracted, false);
                }
            }
        }

        int remaining = extracted;
        int receiverCount = receivers.size();
        for (MachineLocation loc : receivers) {
            if (remaining <= 0 || receiverCount <= 0) break;
            BlockEntity be = level.getBlockEntity(loc.pos);
            if (be != null) {
                IEnergyStorage storage = be.getCapability(ForgeCapabilities.ENERGY, loc.side).orElse(null);
                if (storage != null && storage.canReceive()) {
                    int share = remaining / receiverCount;
                    int inserted = storage.receiveEnergy(Math.max(share, 1), false);
                    remaining -= inserted;
                }
            }
            receiverCount--;
        }
    }

    public void addCable(BlockPos pos) {
        cables.add(pos);
    }

    public void addProvider(BlockPos pos, Direction side) {
        providers.add(new MachineLocation(pos, side));
    }

    public void addReceiver(BlockPos pos, Direction side) {
        receivers.add(new MachineLocation(pos, side));
    }

    public Set<BlockPos> getCables() {
        return cables;
    }

    public Level getLevel() {
        return level;
    }
}