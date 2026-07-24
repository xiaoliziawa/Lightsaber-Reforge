package com.fiskmods.lightsabers.common.data;

import com.fiskmods.lightsabers.Lightsabers;
import fiskfille.utils.helper.FiskServerUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.LogicalSide;

import java.lang.reflect.Field;
import java.util.function.Predicate;

public class ALDataInterp<T> extends ALData<T>
{
    public static final ALDataInterp<Float> FORCE_POWER = new ALDataInterp<>(0.0F);
    public static final ALDataInterp<Float> FORCE_POWER_DIFF = new ALDataInterp<Float>(0.0F)
            .setExempt(SAVE_NBT);
    public static final ALDataInterp<Float> FORCE_PUSHING_TIMER = new ALDataInterp<>(0.0F);
    public static final ALDataInterp<Float> DRAIN_LIFE_TIMER = new ALDataInterp<>(0.0F);
    public static final ALDataInterp<Float> RIGHT_ARM_TIMER = new ALDataInterp<>(0.0F);
    public static final ALDataInterp<Float> LEFT_ARM_TIMER = new ALDataInterp<>(0.0F);

    protected final ALData<T> prevData;

    protected ALDataInterp(T defaultValue)
    {
        super(defaultValue);
        prevData = new ALData<T>(defaultValue).setExempt(SAVE_NBT | SYNC_BYTES);
    }

    protected ALDataInterp(T defaultValue, Predicate<Entity> canSet)
    {
        super(defaultValue, canSet);
        prevData = new ALData<T>(defaultValue, canSet).setExempt(SAVE_NBT | SYNC_BYTES);
    }

    @Override
    protected ALDataInterp<T> setExempt(int exempt)
    {
        prevData.setExempt(exempt);
        super.setExempt(exempt);
        return this;
    }

    @Override
    protected ALDataInterp<T> revokePerms(LogicalSide side)
    {
        prevData.revokePerms(side);
        super.revokePerms(side);
        return this;
    }

    @Override
    public void update(Entity entity)
    {
        prevData.setWithoutNotify(entity, get(entity));
    }

    public ALData<T> getPrevData()
    {
        return prevData;
    }

    public T getPrev(Player player)
    {
        return prevData.get(player);
    }

    public T getPrev(Entity entity)
    {
        return prevData.get(entity);
    }

    public T interpolate(Entity entity, float progress)
    {
        if (progress == 1)
        {
            return get(entity);
        }
        else if (ofType(Float.class))
        {
            return typeClass.getType().cast(FiskServerUtils.interpolate((Float) getPrev(entity), (Float) get(entity), progress));
        }
        else if (ofType(Double.class))
        {
            return typeClass.getType().cast(FiskServerUtils.interpolate((Double) getPrev(entity), (Double) get(entity), progress));
        }
        else
        {
            throw new RuntimeException("Cannot interpolate a non-decimal data type!");
        }
    }

    public T interpolate(Entity entity)
    {
        return interpolate(entity, Lightsabers.proxy.getRenderTick());
    }

    @Override
    protected void init(Field field, String name) throws ClassNotFoundException
    {
        super.init(field, name);
        prevData.init(field, "PREV_" + name);
    }
}
