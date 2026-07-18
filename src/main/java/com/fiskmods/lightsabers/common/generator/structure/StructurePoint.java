package com.fiskmods.lightsabers.common.generator.structure;

import java.util.Objects;

public final class StructurePoint {
    public final int posX;
    public int posY;
    public final int posZ;

    public StructurePoint(int x, int y, int z) {
        posX = x;
        posY = y;
        posZ = z;
    }

    public StructurePoint(StructurePoint point) {
        this(point.posX, point.posY, point.posZ);
    }

    @Override
    public boolean equals(Object object) {
        return object instanceof StructurePoint point
                && posX == point.posX
                && posZ == point.posZ;
    }

    @Override
    public int hashCode() {
        return Objects.hash(posX, posZ);
    }
}
