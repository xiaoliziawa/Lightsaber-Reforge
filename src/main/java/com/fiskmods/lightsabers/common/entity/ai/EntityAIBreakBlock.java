package com.fiskmods.lightsabers.common.entity.ai;

import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.Block;

public class EntityAIBreakBlock extends EntityAIBlockInteract {
    private static final int BREAK_TIME = 60;

    private int breakingTime;
    private int previousProgress = -1;

    public EntityAIBreakBlock(Mob entity) {
        super(entity);
    }

    @Override
    public boolean canUse() {
        return super.canUse()
                && entity.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING);
    }

    @Override
    public void start() {
        super.start();
        breakingTime = 0;
    }

    @Override
    public boolean canContinueToUse() {
        return super.canContinueToUse() && breakingTime <= BREAK_TIME;
    }

    @Override
    public void stop() {
        entity.level().destroyBlockProgress(entity.getId(), interactionPos, -1);
    }

    @Override
    public void tick() {
        super.tick();
        breakingTime++;
        int progress = (int) (breakingTime / (float) BREAK_TIME * 10.0F);
        if (progress != previousProgress) {
            entity.level().destroyBlockProgress(entity.getId(), interactionPos, progress);
            previousProgress = progress;
        }

        if (breakingTime == BREAK_TIME
                && entity.level().getDifficulty() == Difficulty.HARD) {
            int stateId = Block.getId(interactionState);
            entity.level().removeBlock(interactionPos, false);
            entity.level().levelEvent(2001, interactionPos, stateId);
        }
    }
}
