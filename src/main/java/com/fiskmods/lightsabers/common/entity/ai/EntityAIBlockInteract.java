package com.fiskmods.lightsabers.common.entity.ai;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraftforge.common.ForgeHooks;

import java.util.EnumSet;

public abstract class EntityAIBlockInteract extends Goal {
    protected final Mob entity;
    protected BlockPos interactionPos = BlockPos.ZERO;
    protected BlockState interactionState;

    private boolean stoppedInteraction;
    private float initialOffsetX;
    private float initialOffsetZ;

    protected EntityAIBlockInteract(Mob entity) {
        this.entity = entity;
        setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (!entity.horizontalCollision
                || !(entity.getNavigation() instanceof GroundPathNavigation navigation)
                || !navigation.canOpenDoors()) {
            return false;
        }

        Path path = navigation.getPath();
        if (path != null && !path.isDone()) {
            int startIndex = path.getNextNodeIndex();
            int endIndex = Math.min(startIndex + 2, path.getNodeCount());
            for (int index = startIndex; index < endIndex; index++) {
                BlockPos candidate = path.getNodePos(index).above();
                if (entity.distanceToSqr(
                        candidate.getX(),
                        entity.getY(),
                        candidate.getZ()
                ) <= 2.25D && setInteractionBlock(candidate)) {
                    return true;
                }
            }
        }

        return setInteractionBlock(BlockPos.containing(
                entity.getX(),
                entity.getY() + 1.0D,
                entity.getZ()
        ));
    }

    @Override
    public boolean canContinueToUse() {
        return !stoppedInteraction;
    }

    @Override
    public void start() {
        stoppedInteraction = false;
        initialOffsetX = (float) (interactionPos.getX() + 0.5D - entity.getX());
        initialOffsetZ = (float) (interactionPos.getZ() + 0.5D - entity.getZ());
    }

    @Override
    public void tick() {
        float offsetX = (float) (interactionPos.getX() + 0.5D - entity.getX());
        float offsetZ = (float) (interactionPos.getZ() + 0.5D - entity.getZ());
        if (initialOffsetX * offsetX + initialOffsetZ * offsetZ < 0.0F) {
            stoppedInteraction = true;
        }
    }

    private boolean setInteractionBlock(BlockPos pos) {
        BlockState state = entity.level().getBlockState(pos);
        if (state.isAir()
                || state.getDestroySpeed(entity.level(), pos) < 0.0F
                || !ForgeHooks.canEntityDestroy(entity.level(), pos, entity)) {
            return false;
        }
        interactionPos = pos.immutable();
        interactionState = state;
        return true;
    }
}
