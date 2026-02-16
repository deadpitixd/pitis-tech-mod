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

    public void tick() {
        if (providers.isEmpty() || receivers.isEmpty()) return;

        long totalExtractable = 0;
        for (MachineLocation loc : providers) {
            BlockEntity be = level.getBlockEntity(loc.pos);
            if (be != null) {
                IEnergyStorage storage = be.getCapability(ForgeCapabilities.ENERGY, loc.side).orElse(null);
                if (storage != null && storage.canExtract()) {
                    int extractable = storage.extractEnergy(Integer.MAX_VALUE, true);
                    if (extractable > 0) {
                        totalExtractable = Math.min(totalExtractable + extractable, Integer.MAX_VALUE);
                    }
                }
            }
        }

        long totalDemand = 0;
        for (MachineLocation loc : receivers) {
            BlockEntity be = level.getBlockEntity(loc.pos);
            if (be != null) {
                IEnergyStorage storage = be.getCapability(ForgeCapabilities.ENERGY, loc.side).orElse(null);
                if (storage != null && storage.canReceive()) {
                    int demand = storage.receiveEnergy(Integer.MAX_VALUE, true);
                    if (demand > 0) {
                        totalDemand = Math.min(totalDemand + demand, Integer.MAX_VALUE);
                    }
                }
            }
        }

        int toTransfer = (int) Math.min(totalExtractable, totalDemand);
        if (toTransfer <= 0) return;

        System.out.println("Network transferring: " + toTransfer + " FE");

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
            if (remaining <= 0) break;
            BlockEntity be = level.getBlockEntity(loc.pos);
            if (be != null) {
                IEnergyStorage storage = be.getCapability(ForgeCapabilities.ENERGY, loc.side).orElse(null);
                if (storage != null && storage.canReceive()) {
                    int share = Math.max(1, remaining / receiverCount);
                    int inserted = storage.receiveEnergy(Math.min(remaining, share), false);
                    remaining -= inserted;
                    receiverCount--;
                }
            }
        }
    }

    public void addCable(BlockPos pos) { cables.add(pos); }
    public void addProvider(BlockPos pos, Direction side) { providers.add(new MachineLocation(pos, side)); }
    public void addReceiver(BlockPos pos, Direction side) { receivers.add(new MachineLocation(pos, side)); }
    public Set<BlockPos> getCables() { return cables; }
    public Level getLevel() { return level; }
}