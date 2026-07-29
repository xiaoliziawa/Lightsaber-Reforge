package com.fiskmods.lightsabers.common.lightsaber;

import com.fiskmods.lightsabers.ALConstants;
import com.fiskmods.lightsabers.Lightsabers;
import com.fiskmods.lightsabers.common.hilt.Hilt;
import com.fiskmods.lightsabers.common.hilt.Hilt.Part;
import com.fiskmods.lightsabers.common.hilt.HiltManager;
import com.fiskmods.lightsabers.common.item.ItemDoubleLightsaber;
import com.fiskmods.lightsabers.common.item.ItemFocusingCrystal;
import com.fiskmods.lightsabers.common.item.ModItems;
import com.fiskmods.lightsabers.saberbuilder.AbstractLightsaberData;
import fiskfille.utils.helper.FiskComparators;
import fiskfille.utils.helper.NBTHelper.ISaveAdapter;
import fiskfille.utils.helper.NBTHelper.ISerializableObject;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class LightsaberData extends AbstractLightsaberData implements ISerializableObject<LightsaberData>
{
    public static final LightsaberData EMPTY = new LightsaberData();
    public static final float MIN_LENGTH_CM = 19;
    private static final float SPEAR_LENGTH_MULTIPLIER = 10.0F;

    public LightsaberData()
    {
        this(0);
    }

    public LightsaberData(long hashCode)
    {
        hash = hashCode;
    }

    @Override
    protected int getIDForObject(Hilt hilt)
    {
        return Hilt.REGISTRY.getIDForObject(hilt);
    }

    @Override
    protected Hilt getObjectById(int id)
    {
        return Hilt.REGISTRY.getObjectById(id);
    }

    @Override
    protected LightsaberData createNew(long hashCode)
    {
        return new LightsaberData(hashCode);
    }

    /**
     * Creates a new LightsaberData object identical to this one.
     *
     * @return this
     */
    @Override
    public LightsaberData copy()
    {
        return (LightsaberData) super.copy();
    }

    /**
     * Strips this object's hashCode of any unused bits.
     *
     * @return this
     */
    @Override
    public LightsaberData strip()
    {
        return (LightsaberData) super.strip();
    }

    /**
     * @return true if this hilt is shorter than allowed.
     */
    public boolean isTooShort()
    {
        return getHeightCm() < MIN_LENGTH_CM;
    }

    @Override
    public float getHeightCm()
    {
        float heightCm = super.getHeightCm();
        return isSpear() ? heightCm * SPEAR_LENGTH_MULTIPLIER : heightCm;
    }

    public boolean supportsDoubleLightsaber()
    {
        for (PartType type : PartType.values())
        {
            if (!get(type).supportsDoubleLightsaber())
            {
                return false;
            }
        }
        return true;
    }

    public boolean hasSpinningCore()
    {
        return get(PartType.BODY) == HiltManager.SPINNING
                && get(PartType.EMITTER) == HiltManager.SPINNING
                && get(PartType.SWITCH_SECTION) == HiltManager.SPINNING;
    }

    public boolean canSpinBlades()
    {
        return hasSpinningCore() && get(PartType.POMMEL) == HiltManager.SPINNING;
    }

    public boolean isSpear()
    {
        return isUniform(HiltManager.SPEAR);
    }

    private boolean isUniform(Hilt hilt)
    {
        for (PartType type : PartType.values())
        {
            if (get(type) != hilt)
            {
                return false;
            }
        }
        return true;
    }

    public boolean isAssemblyCompatible()
    {
        if (get(PartType.BODY) == HiltManager.SPINNING)
        {
            return hasSpinningCore();
        }

        for (PartType type : PartType.values())
        {
            Hilt hilt = get(type);
            if (hilt == HiltManager.SPINNING)
            {
                return false;
            }
            if (hilt.requiresUniformAssembly() && !isUniform(hilt))
            {
                return false;
            }
        }
        return true;
    }

    /**
     * @param itemstack - the {@link ItemStack} representing this lightsaber
     * @return An array containing the RGB components for the blade of this lightsaber.
     */
    public float[] getRGB(ItemStack itemstack)
    {
        if (getColor() == CrystalColor.RGB)
        {
            return getColor().getRenderRGB();
        }

        if (itemstack.getHoverName().getString().equals("jeb_"))
        {
            Player player = Lightsabers.proxy.getPlayer();

            if (player == null)
            {
                return getColor().getRGB();
            }

            int time = 25;

            float[][] rgb = CrystalColor.COLOR_VALUES;
            float f = (player.tickCount % time + Lightsabers.proxy.getRenderTick()) / time;
            int i = player.tickCount / time;
            int colorCount = CrystalColor.RGB.ordinal();
            int j = i % colorCount;
            int k = (i + 1) % colorCount;

            return new float[] {rgb[j][0] * (1 - f) + rgb[k][0] * f, rgb[j][1] * (1 - f) + rgb[k][1] * f, rgb[j][2] * (1 - f) + rgb[k][2] * f};
        }

        return getColor().getRGB();
    }

    /**
     * Sets the {@link Hilt} design of the lightsaber component in the given {@link PartType} slot.
     *
     * @param type - The component slot
     * @param id - The hilt design
     * @return this
     */
    @Override
    public LightsaberData set(PartType type, Hilt hilt)
    {
        return (LightsaberData) super.set(type, hilt);
    }

    /**
     * Sets the hilt design of each component in this lightsaber to the entry in the given array
     * corresponding to that {@link PartType PartType's} index. <br>
     * <br>
     * Index 0 = {@link PartType#EMITTER emitter} <br>
     * Index 1 = {@link PartType#SWITCH_SECTION switch section} <br>
     * Index 2 = {@link PartType#BODY body} <br>
     * Index 3 = {@link PartType#POMMEL pommel} <br>
     *
     * @param hilt - The array of {@link Hilt} designs
     * @return this
     */
    @Override
    public LightsaberData set(Hilt... hilt)
    {
        return (LightsaberData) super.set(hilt);
    }

    /**
     * Sets the design of all components in this lightsaber to the given {@link Hilt}.
     *
     * @param hilt - The new hilt design
     * @return this
     * @see LightsaberData#isHiltUniform()
     */
    @Override
    public LightsaberData set(Hilt hilt)
    {
        return (LightsaberData) super.set(hilt);
    }

    /**
     * Sets the color of this lightsaber's blade.
     *
     * @param color - The {@link CrystalColor} to represent the blade's color
     * @return this
     */
    @Override
    public LightsaberData set(CrystalColor color)
    {
        return (LightsaberData) super.set(color);
    }

    /**
     * Sets the {@link FocusingCrystal focusing crystals} contained within this lightsaber. Removes
     * any previously contained crystals and passes down to
     * {@link LightsaberData#add(FocusingCrystal)}.
     *
     * @param crystals - The new array of crystals
     * @return this
     */
    @Override
    public LightsaberData set(FocusingCrystal... crystals)
    {
        return (LightsaberData) super.set(crystals);
    }

    /**
     * Adds a {@link FocusingCrystal} to the lightsaber.
     *
     * @param crystal - The crystal to be added
     * @return this
     */
    @Override
    public LightsaberData add(FocusingCrystal crystal)
    {
        return (LightsaberData) super.add(crystal);
    }

    /**
     * Removes the specified {@link FocusingCrystal}, if present.
     *
     * @param crystal - The crystal to be removed
     * @return this
     */
    @Override
    public LightsaberData remove(FocusingCrystal crystal)
    {
        return (LightsaberData) super.remove(crystal);
    }

    /**
     * Creates a new {@link ItemStack} representing this lightsaber.
     *
     * @return The new item stack.
     */
    public ItemStack create()
    {
        ItemStack itemstack = new ItemStack(ModItems.LIGHTSABER.get());
        itemstack.getOrCreateTag().putLong(ALConstants.TAG_LIGHTSABER, strip().hash);

        return itemstack;
    }

    @Override
    public Tag writeToNBT()
    {
        return LongTag.valueOf(hash);
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        buf.writeLong(hash);
    }

    public static class Adapter implements ISaveAdapter<LightsaberData>
    {
        @Override
        public LightsaberData readFromNBT(Tag tag)
        {
            if (tag instanceof NumericTag numericTag)
            {
                return new LightsaberData(numericTag.getAsLong());
            }

            return null;
        }

        @Override
        public LightsaberData fromBytes(ByteBuf buf)
        {
            return new LightsaberData(buf.readLong());
        }
    }

    /**
     * Creates a new LightsaberData object to represent the data contained within the passed
     * {@link CompoundTag NBT}.
     *
     * @param nbt - The tag compound containing the data
     * @return A new object, or {@link LightsaberData#EMPTY} if nbt contains no relevant data.
     * @see ItemStack#getTag()
     */
    public static LightsaberData readFromNBT(CompoundTag nbt)
    {
        if (nbt.contains("Lightsaber", Tag.TAG_COMPOUND))
        {
            CompoundTag compound = nbt.getCompound("Lightsaber");
            LightsaberData data = new LightsaberData().set(CrystalColor.get(compound.getInt("color")));

            if (compound.contains("FocusingCrystals", Tag.TAG_INT_ARRAY))
            {
                for (int id : compound.getIntArray("FocusingCrystals"))
                {
                    data.add(ItemFocusingCrystal.get(id));
                }
            }

            for (PartType type : PartType.values())
            {
                data.set(type, Hilt.REGISTRY.getObject(Hilt.LEGACY_MAPPINGS.get(compound.getString(type.name().toLowerCase(Locale.ROOT)))));
            }
            
            nbt.remove("Lightsaber");
            nbt.putLong(ALConstants.TAG_LIGHTSABER, data.hash);

            return data;
        }
        else if (nbt.contains(ALConstants.TAG_LIGHTSABER, Tag.TAG_ANY_NUMERIC))
        {
            return new LightsaberData(nbt.getLong(ALConstants.TAG_LIGHTSABER)).strip();
        }
        else if (nbt.contains(ALConstants.TAG_LIGHTSABER, Tag.TAG_STRING))
        {
            try
            {
                return new LightsaberData(Long.decode(nbt.getString(ALConstants.TAG_LIGHTSABER))).strip();
            }
            catch (NumberFormatException e)
            {
            }
        }

        return EMPTY;
    }

    /**
     * Retrieves a LightsaberData object representing the given {@link ItemStack}.
     *
     * @param itemstack - The item stack to be represented
     * @return The LightsaberData representation.
     * @see LightsaberData#readFromNBT(CompoundTag)
     */
    public static LightsaberData get(ItemStack itemstack)
    {
        if (itemstack != null && !itemstack.isEmpty() && itemstack.hasTag())
        {
            return readFromNBT(itemstack.getTag());
        }

        return EMPTY;
    }

    /**
     * @see LightsaberData#get(PartType)
     * @see LightsaberData#get(ItemStack)
     */
    public static Hilt get(ItemStack itemstack, PartType type)
    {
        return get(itemstack).get(type);
    }

    /**
     * @see LightsaberData#getHilt(ItemStack)
     * @see LightsaberData#get(ItemStack)
     */
    public static Hilt[] getHilt(ItemStack itemstack)
    {
        return get(itemstack).getHilt();
    }

    /**
     * @see LightsaberData#getPart(PartType)
     * @see LightsaberData#get(ItemStack)
     */
    public static Part getPart(ItemStack itemstack, PartType type)
    {
        return get(itemstack).getPart(type);
    }

    /**
     * @see LightsaberData#getHeight()
     * @see LightsaberData#get(ItemStack)
     */
    public static float getHeight(ItemStack itemstack)
    {
        if (itemstack.getItem() == ModItems.DOUBLE_LIGHTSABER.get())
        {
            LightsaberData[] array = ItemDoubleLightsaber.get(itemstack);
            return array[0].getHeight() + array[1].getHeight();
        }

        return get(itemstack).getHeight();
    }

    /**
     * @see LightsaberData#getHeightCm()
     * @see LightsaberData#get(ItemStack)
     */
    public static float getHeightCm(ItemStack itemstack)
    {
        if (itemstack.getItem() == ModItems.DOUBLE_LIGHTSABER.get())
        {
            float heightCm = 0.0F;
            for (LightsaberData data : ItemDoubleLightsaber.get(itemstack))
            {
                heightCm += data.getHeightCm();
            }
            return heightCm;
        }

        return get(itemstack).getHeightCm();
    }

    /**
     * @see LightsaberData#getColor()
     * @see LightsaberData#get(ItemStack)
     */
    public static CrystalColor getColor(ItemStack itemstack)
    {
        return get(itemstack).getColor();
    }

    /**
     * @see LightsaberData#getFocusingCrystals()
     * @see LightsaberData#get(ItemStack)
     */
    public static FocusingCrystal[] getFocusingCrystals(ItemStack itemstack)
    {
        return get(itemstack).getFocusingCrystals();
    }

    /**
     * @see LightsaberData#hasFocusingCrystal(FocusingCrystal)
     * @see LightsaberData#get(ItemStack)
     */
    public static boolean hasFocusingCrystal(ItemStack itemstack, FocusingCrystal crystal)
    {
        return get(itemstack).hasFocusingCrystal(crystal);
    }

    /**
     * Creates a new random lightsaber {@link ItemStack} of the given color.
     *
     * @param rand - The random instance
     * @param color - The blade color
     * @return The new item
     */
    public static ItemStack createRandom(Random rand, CrystalColor color)
    {
        LightsaberData data;
        do
        {
            data = new LightsaberData().set(createRandomHilt(rand));
        }
        while (data.isTooShort() || !data.isAssemblyCompatible());

        if (color == null)
        {
            color = CrystalColor.getRandom(rand);
        }

        data.set(color);

        if (rand.nextInt(10) == 0)
        {
            List<FocusingCrystal> crystals = new ArrayList<>(Arrays.asList(FocusingCrystal.values()));
            Collections.sort(crystals, FiskComparators.random(rand));

            data.add(crystals.get(0));

            if (rand.nextInt(20) == 0)
            {
                data.add(crystals.get(1));
            }
        }

        return data.create();
    }

    private static Hilt[] createRandomHilt(Random rand)
    {
        Hilt body = Hilt.REGISTRY.getRandom(rand);
        Hilt[] hilt = new Hilt[PartType.values().length];

        if (requiresUniformRandomAssembly(body))
        {
            Arrays.fill(hilt, body);
            return hilt;
        }

        for (PartType type : PartType.values())
        {
            hilt[type.ordinal()] = type == PartType.BODY
                    ? body
                    : getRandomCombinableHilt(rand);
        }
        return hilt;
    }

    private static Hilt getRandomCombinableHilt(Random rand)
    {
        Hilt hilt;
        do
        {
            hilt = Hilt.REGISTRY.getRandom(rand);
        }
        while (requiresUniformRandomAssembly(hilt));
        return hilt;
    }

    private static boolean requiresUniformRandomAssembly(Hilt hilt)
    {
        return hilt == HiltManager.SPINNING || hilt.requiresUniformAssembly();
    }

    /**
     * Creates a new random lightsaber {@link ItemStack} of a random color.
     *
     * @param rand - The random instance
     * @return The new item
     * @see LightsaberData#createRandom(Random)
     */
    public static ItemStack createRandom(Random rand)
    {
        return createRandom(rand, null);
    }
}
