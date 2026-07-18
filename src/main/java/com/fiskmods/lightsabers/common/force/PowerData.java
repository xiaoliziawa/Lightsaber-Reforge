package com.fiskmods.lightsabers.common.force;

import com.fiskmods.lightsabers.common.data.ALData;
import com.fiskmods.lightsabers.common.data.ALData.DataFactory;
import fiskfille.utils.helper.NBTHelper;
import fiskfille.utils.helper.NBTHelper.ISaveAdapter;
import fiskfille.utils.helper.NBTHelper.ISerializableObject;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.Entity;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class PowerData implements Comparable<PowerData>, ISerializableObject<PowerData>
{
    public final Power power;
    public int xpInvested;
    
    private boolean unlocked;

    public PowerData(Power p)
    {
        power = p;
    }
    
    public boolean isUnlocked()
    {
        return unlocked;
    }
    
    public void setUnlocked(Entity entity, boolean b)
    {
        if (b != unlocked)
        {
            unlocked = b;
            ALData.POWERS.get(entity).markDirty(entity);
        }
    }
    
    @Override
    public int hashCode()
    {
        int hashCode = power.hashCode();
        hashCode = hashCode * 31 + (unlocked ? 1 : 0);
        hashCode = hashCode * 31 + xpInvested;
        
        return hashCode;
    }
    
    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof PowerData)
        {
            PowerData data = (PowerData) obj;
            
            return power == data.power && unlocked == data.unlocked && xpInvested == data.xpInvested;
        }
        
        return false;
    }
    
    @Override
    public String toString()
    {
        return String.format("PowerData[\"%s\",x=%s,xp=%s]", power.getName(), unlocked, xpInvested);
    }
    
    @Override
    public int compareTo(PowerData o)
    {
        return unlocked && !o.unlocked ? 1 : power.compareTo(o.power);
    }

    @Override
    public Tag writeToNBT()
    {
        CompoundTag nbt = new CompoundTag();
        nbt.putString("Id", power.getName());
        nbt.putBoolean("Unlocked", unlocked);
        nbt.putInt("XpInvested", xpInvested);

        return nbt;
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        NBTHelper.toBytes(buf, power.getName());
        buf.writeBoolean(unlocked);
        buf.writeInt(xpInvested);
    }
    
    public static class Adapter implements ISaveAdapter<PowerData>
    {
        @Override
        public PowerData readFromNBT(Tag tag)
        {
            if (tag instanceof CompoundTag nbt)
            {
                Power power = Power.getPowerFromName(nbt.getString("Id"));
                if (power == null)
                {
                    return null;
                }
                PowerData data = new PowerData(power);
                data.unlocked = nbt.getBoolean("Unlocked");
                data.xpInvested = nbt.getInt("XpInvested");

                return data;
            }

            return null;
        }

        @Override
        public PowerData fromBytes(ByteBuf buf)
        {
            Power power = Power.getPowerFromName(NBTHelper.fromBytes(buf, String.class));
            if (power == null)
            {
                return null;
            }
            PowerData data = new PowerData(power);
            data.unlocked = buf.readBoolean();
            data.xpInvested = buf.readInt();

            return data;
        }
    }
    
    public static class Container extends HashMap<Power, PowerData> implements ISerializableObject<Container>, Iterable<PowerData>
    {
        private int forceMax;
        private byte basePower;
        private float regen;
        
        private boolean dirty = true;
        
        private Container(Map<? extends Power, ? extends PowerData> map)
        {
            super(map);
        }
        
        private Container()
        {
        }
        
        public int getForceMax()
        {
            return forceMax;
        }
        
        public byte getBasePower()
        {
            return basePower;
        }
        
        public float getRegen()
        {
            return regen;
        }
        
        public void update(Entity entity)
        {
            if (dirty)
            {
                markDirty(entity);
                dirty = false;
            }
        }
        
        public void markDirty(Entity entity)
        {
            forceMax = 0;
            basePower = 0;
            regen = 0;

            for (PowerData data : this)
            {
                if (data.unlocked)
                {
                    PowerStats stats = data.power.powerStats;

                    if (stats.regenOperation == 0)
                    {
                        regen += stats.regen;
                    }
                    else
                    {
                        regen += stats.regen / 100F * regen;
                    }
                    
                    forceMax += stats.forceBonus;
                    basePower += stats.baseBonus;
                    basePower -= stats.baseRequirement;
                }
            }
        }
        
        public PowerData add(PowerData value)
        {
            return put(value.power, value);
        }
        
        public Container validate()
        {
            for (Power power : Power.POWERS)
            {
                if (!containsKey(power))
                {
                    add(new PowerData(power));
                }
            }
            
            dirty = true;
            
            return this;
        }
        
        @Override
        public Iterator<PowerData> iterator()
        {
            return values().iterator();
        }

        @Override
        public Tag writeToNBT()
        {
            ListTag list = new ListTag();
            
            for (PowerData data : this)
            {
                list.add(NBTHelper.writeToNBT(data));
            }

            return list;
        }

        @Override
        public void toBytes(ByteBuf buf)
        {
            buf.writeInt(size());
            
            for (PowerData data : this)
            {
                NBTHelper.toBytes(buf, data);
            }
        }
        
        public static class Adapter implements ISaveAdapter<Container>
        {
            @Override
            public Container readFromNBT(Tag tag)
            {
                if (tag instanceof ListTag list)
                {
                    Container container = new Container(new HashMap<>());

                    for (Tag entry : list)
                    {
                        PowerData data = NBTHelper.readFromNBT(entry, PowerData.class);

                        if (data != null)
                        {
                            container.add(data);
                        }
                    }
                    
                    return container.validate();
                }

                return null;
            }

            @Override
            public Container fromBytes(ByteBuf buf)
            {
                Container container = new Container(new HashMap<>());
                int length = buf.readInt();
                
                for (int i = 0; i < length; ++i)
                {
                    PowerData data = NBTHelper.fromBytes(buf, PowerData.class);
                    if (data != null)
                    {
                        container.add(data);
                    }
                }
                
                return container.validate();
            }
        }

        public static DataFactory<Container> factory()
        {
            return new DataFactory<Container>()
            {
                @Override
                public Container construct()
                {
                    return new Container().validate();
                }
                
                @Override
                public boolean canEqual()
                {
                    return false;
                }
            };
        }
    }
}
