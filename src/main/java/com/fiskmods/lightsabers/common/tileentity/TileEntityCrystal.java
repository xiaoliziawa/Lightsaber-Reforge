package com.fiskmods.lightsabers.common.tileentity;

import com.fiskmods.lightsabers.common.lightsaber.CrystalColor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Random;

public class TileEntityCrystal extends BlockEntity {
    private static final String COLOR_TAG = "color";

    private CrystalColor crystalColor = CrystalColor.DEEP_BLUE;
    private final int renderRotation;
    private final float renderOffset;

    public TileEntityCrystal(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CRYSTAL.get(), pos, state);
        Random random = new Random((long) pos.getX() + pos.getY() + pos.getZ());
        renderRotation = random.nextInt(360);
        renderOffset = random.nextInt(10) / 40.0F;
    }

    public void setColor(CrystalColor color) {
        if (crystalColor == color) {
            return;
        }

        crystalColor = color;
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(
                    worldPosition,
                    getBlockState(),
                    getBlockState(),
                    Block.UPDATE_CLIENTS
            );
        }
    }

    public CrystalColor getColor() {
        return crystalColor;
    }

    public int getRenderRotation() {
        return renderRotation;
    }

    public float getRenderOffset() {
        return renderOffset;
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        crystalColor = CrystalColor.get(tag.getInt(COLOR_TAG));
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt(COLOR_TAG, crystalColor.id);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }
}
