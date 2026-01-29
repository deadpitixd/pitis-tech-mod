package com.piti.ptm.block.entity;

import com.piti.ptm.screen.LavaHeaterMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LavaHeaterBlockEntity extends BlockEntity implements MenuProvider {
    private final ItemStackHandler itemHandler = new ItemStackHandler(2);

    private static final int LAVA_SLOT = 0;
    private static final int MAGMA_SLOT = 1;

    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();

    protected final ContainerData data;

    public ContainerData getContainerData() {
        return this.data;
    }

    private int temp = 0;
    private int maxTemp = 2500;
    private int lava = 0;
    private int maxLava = 5000;
    private int water = 0;
    private int maxWater = 10000;
    private int steam = 0;
    private int maxSteam = 8000;

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if(cap == ForgeCapabilities.ITEM_HANDLER){
            return lazyItemHandler.cast();
        }
        return super.getCapability(cap, side);
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

    public void drops(){
        SimpleContainer inventory = new SimpleContainer(itemHandler.getSlots());
        for (int i = 0; i < itemHandler.getSlots(); i++){
            inventory.setItem(i, itemHandler.getStackInSlot(i));
        }
        Containers.dropContents(this.level, this.worldPosition, inventory);
    }


    public LavaHeaterBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(ModBlockEntities.LAVA_HEATER_BE.get(), pPos, pBlockState);
        this.data = new ContainerData() {
            @Override
            public int get(int pIndex) {
                return switch (pIndex) {
                    case 0 -> LavaHeaterBlockEntity.this.lava;
                    case 1 -> LavaHeaterBlockEntity.this.maxLava;
                    case 2 -> LavaHeaterBlockEntity.this.water;
                    case 3 -> LavaHeaterBlockEntity.this.maxWater;
                    case 4 -> LavaHeaterBlockEntity.this.temp;
                    case 5 -> LavaHeaterBlockEntity.this.maxTemp;
                    case 6 -> LavaHeaterBlockEntity.this.steam;
                    case 7 -> LavaHeaterBlockEntity.this.maxSteam;
                    default -> 0;
                };
            }

            @Override
            public void set(int pIndex, int pValue) {
                switch (pIndex){
                    case 0 -> LavaHeaterBlockEntity.this.lava = pValue;
                    case 1 -> LavaHeaterBlockEntity.this.maxLava = pValue;
                    case 2 -> LavaHeaterBlockEntity.this.water = pValue;
                    case 3 -> LavaHeaterBlockEntity.this.maxWater = pValue;
                    case 4 -> LavaHeaterBlockEntity.this.temp = pValue;
                    case 5 -> LavaHeaterBlockEntity.this.maxTemp = pValue;
                    case 6 -> LavaHeaterBlockEntity.this.steam = pValue;
                    case 7 -> LavaHeaterBlockEntity.this.maxSteam = pValue;
                }
            }


            @Override
            public int getCount() {
                return 8;
            }
        };


    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.ptm.lava_heater");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
        return new LavaHeaterMenu(pContainerId, pPlayerInventory, this, this.data);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("inventory", itemHandler.serializeNBT());

        tag.putInt("lava", lava);
        tag.putInt("water", water);
        tag.putInt("steam", steam);
        tag.putInt("temp", temp);

        tag.putInt("maxLava", maxLava);
        tag.putInt("maxWater", maxWater);
        tag.putInt("maxSteam", maxSteam);
        tag.putInt("maxTemp", maxTemp);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        itemHandler.deserializeNBT(tag.getCompound("inventory"));

        lava = tag.getInt("lava");
        water = tag.getInt("water");
        steam = tag.getInt("steam");
        temp = tag.getInt("temp");

        maxLava = tag.getInt("maxLava");
        maxWater = tag.getInt("maxWater");
        maxSteam = tag.getInt("maxSteam");
        maxTemp = tag.getInt("maxTemp");
    }



    public static void tick(Level level, BlockPos pos, BlockState state, LavaHeaterBlockEntity be) {
        boolean changed = false;

        if (be.hasRecipe()) {
            be.exchangeLava();
            changed = true;
        }

        if (be.lava > 5) {
            be.lava -= 5;
            be.temp = Math.min(be.temp + 500, be.maxTemp);
            changed = true;
        }

        // Generate steam from water
        if (be.temp > 150 && be.water > 5) {
            be.steam = Math.min(be.steam + 5, be.maxSteam);
            be.water -= 5;
            be.temp -= 1;
            changed = true;
        }

        if (be.temp > 0 && be.lava <= 5) {
            be.temp = Math.max(be.temp - 1, 0);
            changed = true;
        }

        be.data.set(0, be.lava);
        be.data.set(2, be.water);
        be.data.set(4, be.temp);
        be.data.set(6, be.steam);

        if (changed) {
            be.setChanged();
        }
    }



    private void exchangeLava() {
        ItemStack result = new ItemStack(Items.MAGMA_BLOCK, 1);
        this.itemHandler.extractItem(LAVA_SLOT, 1, false);

        this.lava += 1000;

        this.itemHandler.setStackInSlot(LAVA_SLOT, new ItemStack(Items.BUCKET, 1));

        this.itemHandler.setStackInSlot(MAGMA_SLOT, new ItemStack(result.getItem(),
                this.itemHandler.getStackInSlot(MAGMA_SLOT).getCount() + result.getCount()));
    }

    private boolean hasRecipe() {
        boolean hasCraftingItem = this.itemHandler.getStackInSlot(LAVA_SLOT).getItem() == Items.LAVA_BUCKET;
        ItemStack magma = new ItemStack(Items.MAGMA_BLOCK);

        return hasCraftingItem && canInsertIntoOutputSlot(magma.getCount()) && canInsertItemIntoOutputSlot(magma.getItem());

    }

    private boolean canInsertIntoOutputSlot(int count) {
        return this.itemHandler.getStackInSlot(MAGMA_SLOT).getCount() + count <= this.itemHandler.getStackInSlot(MAGMA_SLOT).getMaxStackSize();
    }

    private boolean canInsertItemIntoOutputSlot(Item item){
        return this.itemHandler.getStackInSlot(MAGMA_SLOT).isEmpty() || this.itemHandler.getStackInSlot(MAGMA_SLOT).is(item);
    }
}
