package com.piti.ptm.block.entity;

import com.piti.ptm.block.entity.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class lavaHeaterBlockEntity extends BlockEntity {

    private final ItemStackHandler itemHandler = new ItemStackHandler(2); // input + lava bucket
    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();

    protected final ContainerData data;

    private int steam;
    private int water;
    private int TU;
    private int lava;

    private static int MAX_TU = 250;
    private static int MAX_WATER = 10000;
    private static int MAX_STEAM = 10000;
    private static int MAX_LAVA = 5000;

    public lavaHeaterBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.LAVA_HEATER_BE.get(), pos, state);

        this.data = new ContainerData() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case 0 -> MAX_STEAM;
                    case 1 -> MAX_TU;
                    case 2 -> MAX_WATER;
                    case 3 -> MAX_LAVA;
                    case 4 -> water;
                    case 5 -> steam;
                    case 6 -> TU;
                    case 7 -> lava;
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
                switch (index) {
                    case 0 -> MAX_STEAM = value;
                    case 1 -> MAX_TU = value;
                    case 2 -> MAX_WATER = value;
                    case 3 -> MAX_LAVA = value;
                    case 4 -> water = value;
                    case 5 -> steam = value;
                    case 6 -> TU = value;
                    case 7 -> lava = value;
                }
            }

            @Override
            public int getCount() {
                return 8;
            }
        };
    }

    public static void tick(Level level, BlockPos pos, BlockState state, lavaHeaterBlockEntity entity) {
        if (!level.isClientSide) {
            entity.TU += 1;
        }
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) return lazyItemHandler.cast();
        return super.getCapability(cap);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        lazyItemHandler = LazyOptional.of(() -> itemHandler);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        lazyItemHandler.invalidate();
    }

    public void drops() {
        SimpleContainer inventory = new SimpleContainer(itemHandler.getSlots());
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            inventory.setItem(i, itemHandler.getStackInSlot(i));
        }
        Containers.dropContents(level, worldPosition, inventory);
    }

    // This tick method will update lava -> TU -> steam conversion every tick
    public void tick() {
        if (level == null || level.isClientSide()) return;

        // Example logic: heat lava -> generate TU
        int lavaUsed = Math.min(lava, 10);
        lava -= lavaUsed;
        TU += lavaUsed * 5;
        if (TU > MAX_TU) TU = MAX_TU;

        // Example: TU converts water -> steam
        int waterUsed = Math.min(water, TU / 10);
        water -= waterUsed;
        steam += waterUsed * 10;
        if (steam > MAX_STEAM) steam = MAX_STEAM;

        // Sync block entity to client every tick
        setChanged();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("inventory", itemHandler.serializeNBT());
        tag.putInt("steam", steam);
        tag.putInt("water", water);
        tag.putInt("tu", TU);
        tag.putInt("lava", lava);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        itemHandler.deserializeNBT(tag.getCompound("inventory"));
        steam = tag.getInt("steam");
        water = tag.getInt("water");
        TU = tag.getInt("tu");
        lava = tag.getInt("lava");
    }
}
