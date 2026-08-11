package com.fiskmods.lightsabers.common.force.effect;

import fiskfille.utils.helper.VectorHelper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public final class ForceTargeting {
    private static final double SEARCH_INFLATION = 1.0D;

    private ForceTargeting() {
    }

    public static LivingEntity findLookTarget(Player player, double range) {
        Vec3 start = VectorHelper.getOffsetCoords(player, 0, 0, 0);
        Vec3 end = VectorHelper.getOffsetCoords(player, 0, 0, range);
        BlockHitResult blockHit = player.level().clip(new ClipContext(
                start,
                end,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                player
        ));
        Vec3 limit = blockHit.getType() == HitResult.Type.MISS ? end : blockHit.getLocation();
        AABB searchBox = player.getBoundingBox()
                .expandTowards(limit.subtract(start))
                .inflate(SEARCH_INFLATION);
        EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(
                player,
                start,
                limit,
                searchBox,
                entity -> entity instanceof LivingEntity
                        && entity != player
                        && entity != player.getVehicle()
                        && !entity.isSpectator()
                        && entity.isPickable(),
                start.distanceToSqr(limit)
        );
        return entityHit != null && entityHit.getEntity() instanceof LivingEntity livingEntity
                ? livingEntity
                : null;
    }
}
