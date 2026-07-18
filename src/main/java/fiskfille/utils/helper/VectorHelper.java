package fiskfille.utils.helper;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public final class VectorHelper {
    private static final float DEGREES_TO_RADIANS = (float) Math.PI / 180.0F;

    private VectorHelper() {
    }

    public static Vec3 getOffsetCoords(
            LivingEntity entity,
            double xOffset,
            double yOffset,
            double zOffset,
            float partialTicks
    ) {
        float pitch = Mth.lerp(partialTicks, entity.xRotO, entity.getXRot());
        float yaw = Mth.lerp(partialTicks, entity.yRotO, entity.getYRot());
        Vec3 offset = new Vec3(xOffset, yOffset, zOffset)
                .xRot(-pitch * DEGREES_TO_RADIANS)
                .yRot(-yaw * DEGREES_TO_RADIANS);
        return getPosition(entity, partialTicks).add(0, getOffset(entity), 0).add(offset);
    }

    public static Vec3 getOffsetCoords(
            LivingEntity entity,
            double xOffset,
            double yOffset,
            double zOffset
    ) {
        return getOffsetCoords(entity, xOffset, yOffset, zOffset, 1.0F);
    }

    public static Vec3 copy(Vec3 vector) {
        return new Vec3(vector.x, vector.y, vector.z);
    }

    public static Vec3 add(Vec3 first, Vec3 second) {
        return first.add(second);
    }

    public static Vec3 multiply(Vec3 first, Vec3 second) {
        return first.multiply(second);
    }

    public static Vec3 multiply(Vec3 vector, double factor) {
        return vector.scale(factor);
    }

    public static Vec3 interpolate(Vec3 first, Vec3 second, double distance) {
        double totalDistance = first.distanceTo(second);
        return totalDistance == 0 ? first : second.lerp(first, distance / totalDistance);
    }

    public static Vec3 centerOf(Entity entity) {
        return entity.getBoundingBox().getCenter();
    }

    public static Vec3 getPosition(Entity entity, float partialTicks) {
        return new Vec3(
                Mth.lerp(partialTicks, entity.xo, entity.getX()),
                Mth.lerp(partialTicks, entity.yo, entity.getY()),
                Mth.lerp(partialTicks, entity.zo, entity.getZ())
        );
    }

    public static double getOffset(LivingEntity entity) {
        return entity.getEyeHeight();
    }

    public static Vec3 getBackSideCoordsRenderYawOffset(
            LivingEntity entity,
            double amount,
            boolean side,
            double backAmount,
            boolean pitch
    ) {
        Vec3 origin = entity.position().add(0, getOffset(entity), 0);
        Vec3 front = getFrontCoordsRenderYawOffset(entity, backAmount, pitch).subtract(origin);
        return getSideCoordsRenderYawOffset(entity, amount, side).add(front);
    }

    public static Vec3 getSideCoordsRenderYawOffset(LivingEntity entity, double amount, boolean side) {
        return getSideCoordsRenderYawOffset(entity, amount, side ? -90 : 90);
    }

    public static Vec3 getSideCoordsRenderYawOffset(LivingEntity entity, double amount, int side) {
        return bodyRotationOffset(entity, amount, 0, side);
    }

    public static Vec3 getFrontCoordsRenderYawOffset(
            LivingEntity entity,
            double amount,
            boolean applyPitch
    ) {
        return bodyRotationOffset(entity, amount, applyPitch ? entity.getXRot() : 0, 0);
    }

    private static Vec3 bodyRotationOffset(
            LivingEntity entity,
            double amount,
            float pitch,
            int yawOffset
    ) {
        float yaw = entity.yBodyRot + yawOffset;
        float yawCos = Mth.cos(-yaw * DEGREES_TO_RADIANS - (float) Math.PI);
        float yawSin = Mth.sin(-yaw * DEGREES_TO_RADIANS - (float) Math.PI);
        float pitchCos = -Mth.cos(-pitch * DEGREES_TO_RADIANS);
        double yScale = Mth.sin(-pitch * DEGREES_TO_RADIANS);
        double xScale = yawSin * pitchCos;
        double zScale = yawCos * pitchCos;
        return entity.position()
                .add(0, getOffset(entity), 0)
                .add(xScale * amount, yScale * amount, zScale * amount);
    }

    public static <T extends Entity> List<T> getEntitiesNear(
            Class<T> type,
            Level level,
            double x,
            double y,
            double z,
            double radius
    ) {
        return level.getEntitiesOfClass(
                type,
                new AABB(
                        x - radius,
                        y - radius,
                        z - radius,
                        x + radius,
                        y + radius,
                        z + radius
                )
        );
    }

    public static <T extends Entity> List<T> getEntitiesNear(
            Class<T> type,
            Level level,
            Vec3 position,
            double radius
    ) {
        return getEntitiesNear(type, level, position.x, position.y, position.z, radius);
    }
}
