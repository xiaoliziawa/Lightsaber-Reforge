package fiskfille.utils;

import com.fiskmods.lightsabers.common.data.ALData.DataFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.Objects;

public class DimensionalCoords implements Comparable<DimensionalCoords> {
    public int posX;
    public int posY;
    public int posZ;
    public ResourceKey<Level> dimension = Level.OVERWORLD;

    public DimensionalCoords() {
    }

    public DimensionalCoords(int x, int y, int z, int legacyDimension) {
        this(x, y, z, fromLegacyDimension(legacyDimension));
    }

    public DimensionalCoords(int x, int y, int z, ResourceKey<Level> dimension) {
        set(x, y, z, dimension);
    }

    public DimensionalCoords(BlockPos pos, ResourceKey<Level> dimension) {
        this(pos.getX(), pos.getY(), pos.getZ(), dimension);
    }

    public DimensionalCoords(BlockEntity blockEntity) {
        set(blockEntity);
    }

    public static DimensionalCoords copy(DimensionalCoords coords) {
        return coords == null ? null : new DimensionalCoords().set(coords);
    }

    public DimensionalCoords set(int x, int y, int z, ResourceKey<Level> dimension) {
        posX = x;
        posY = y;
        posZ = z;
        this.dimension = Objects.requireNonNull(dimension);
        return this;
    }

    public DimensionalCoords set(BlockEntity blockEntity) {
        Level level = blockEntity.getLevel();
        if (level != null) {
            return set(blockEntity.getBlockPos(), level.dimension());
        }
        return this;
    }

    public DimensionalCoords set(BlockPos pos, ResourceKey<Level> dimension) {
        return set(pos.getX(), pos.getY(), pos.getZ(), dimension);
    }

    public DimensionalCoords set(DimensionalCoords coords) {
        return set(coords.posX, coords.posY, coords.posZ, coords.dimension);
    }

    public BlockPos toBlockPos() {
        return new BlockPos(posX, posY, posZ);
    }

    public Identifier dimensionLocation() {
        return dimension.identifier();
    }

    public static DimensionalCoords fromLegacyArray(int[] values) {
        int[] normalized = new int[4];
        System.arraycopy(values, 0, normalized, 0, Math.min(values.length, normalized.length));
        return new DimensionalCoords(normalized[0], normalized[1], normalized[2], normalized[3]);
    }

    public static DataFactory<DimensionalCoords> factory() {
        return new DataFactory<>() {
            @Override
            public DimensionalCoords construct() {
                return new DimensionalCoords();
            }
        };
    }

    public static ResourceKey<Level> dimension(Identifier location) {
        return ResourceKey.create(Registries.DIMENSION, location);
    }

    private static ResourceKey<Level> fromLegacyDimension(int dimension) {
        return switch (dimension) {
            case -1 -> Level.NETHER;
            case 1 -> Level.END;
            default -> Level.OVERWORLD;
        };
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof DimensionalCoords coords)) {
            return false;
        }
        return posX == coords.posX
                && posY == coords.posY
                && posZ == coords.posZ
                && dimension.equals(coords.dimension);
    }

    @Override
    public int hashCode() {
        return Objects.hash(posX, posY, posZ, dimension);
    }

    @Override
    public String toString() {
        return "Pos{x=" + posX + ", y=" + posY + ", z=" + posZ
                + ", dimension=" + dimension.identifier() + '}';
    }

    @Override
    public int compareTo(DimensionalCoords coords) {
        int dimensionComparison = dimension.identifier().compareTo(coords.dimension.identifier());
        if (dimensionComparison != 0) {
            return dimensionComparison;
        }
        int yComparison = Integer.compare(posY, coords.posY);
        if (yComparison != 0) {
            return yComparison;
        }
        int zComparison = Integer.compare(posZ, coords.posZ);
        return zComparison != 0 ? zComparison : Integer.compare(posX, coords.posX);
    }
}
