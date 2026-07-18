package com.fiskmods.lightsabers.common.entity.ai;

import com.fiskmods.lightsabers.common.entity.EntitySithGhost;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

public class EntityAIRest extends Goal {
    private final EntitySithGhost entity;
    private final double speed;

    public EntityAIRest(EntitySithGhost entity, double speed) {
        this.entity = entity;
        this.speed = speed;
        setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        return canContinueToUse();
    }

    @Override
    public boolean canContinueToUse() {
        return entity.isReturningToRestingPlace() && entity.hasRestingPlace();
    }

    @Override
    public void start() {
        entity.getNavigation().moveTo(
                entity.getRestingPlaceX(),
                entity.getRestingPlaceY(),
                entity.getRestingPlaceZ(),
                speed
        );
    }
}
