package fiskfille.utils.helper;

import com.fiskmods.lightsabers.common.data.ALData.ClassType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import fiskfille.utils.DimensionalCoords;
import fiskfille.utils.registry.FiskRegistryEntry;
import fiskfille.utils.registry.FiskSimpleRegistry;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.ShortTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class NBTHelper {
    private static final Map<Class<? extends ISerializableObject<?>>, ISaveAdapter<?>> ADAPTERS =
            new HashMap<>();

    private NBTHelper() {
    }

    public static List<Tag> getTags(ListTag tagList) {
        return new ArrayList<>(tagList);
    }

    public static Tag writeToNBT(Object obj) {
        if (obj instanceof ISerializableObject<?> serializable) {
            return serializable.writeToNBT();
        }
        if (obj instanceof Byte value) {
            return ByteTag.valueOf(value);
        }
        if (obj instanceof Short value) {
            return ShortTag.valueOf(value);
        }
        if (obj instanceof Integer value) {
            return IntTag.valueOf(value);
        }
        if (obj instanceof Long value) {
            return LongTag.valueOf(value);
        }
        if (obj instanceof Float value) {
            return FloatTag.valueOf(value);
        }
        if (obj instanceof Double value) {
            return DoubleTag.valueOf(value);
        }
        if (obj instanceof Boolean value) {
            return ByteTag.valueOf(value);
        }
        if (obj instanceof String value) {
            return StringTag.valueOf(value);
        }
        if (obj instanceof List<?> values) {
            ListTag result = new ListTag();
            for (Object value : values) {
                Tag tag = writeToNBT(value);
                if (tag != null) {
                    result.add(tag);
                }
            }
            return result;
        }
        if (obj instanceof ItemStack itemStack) {
            return itemStack.save(new CompoundTag());
        }
        if (obj instanceof DimensionalCoords coords) {
            CompoundTag tag = new CompoundTag();
            tag.putInt("x", coords.posX);
            tag.putInt("y", coords.posY);
            tag.putInt("z", coords.posZ);
            tag.putString("dimension", coords.dimensionLocation().toString());
            return tag;
        }
        return null;
    }

    public static <T> T readFromNBT(Tag tag, Class<T> type) {
        return readFromNBT(tag, new ClassType<>(type));
    }

    @SuppressWarnings("unchecked")
    public static <T> T readFromNBT(Tag tag, ClassType<T> typeClass) {
        Class<T> type = typeClass.getType();
        if (ISerializableObject.class.isAssignableFrom(type)) {
            ISaveAdapter<?> adapter = ADAPTERS.get(type);
            return adapter == null ? null : (T) adapter.readFromNBT(tag);
        }
        if (tag instanceof NumericTag numeric) {
            if (type == Byte.class) return (T) Byte.valueOf(numeric.getAsByte());
            if (type == Short.class) return (T) Short.valueOf(numeric.getAsShort());
            if (type == Integer.class) return (T) Integer.valueOf(numeric.getAsInt());
            if (type == Long.class) return (T) Long.valueOf(numeric.getAsLong());
            if (type == Float.class) return (T) Float.valueOf(numeric.getAsFloat());
            if (type == Double.class) return (T) Double.valueOf(numeric.getAsDouble());
            if (type == Boolean.class) return (T) Boolean.valueOf(numeric.getAsByte() != 0);
        }
        if (type == String.class && tag instanceof StringTag stringTag) {
            return (T) stringTag.getAsString();
        }
        if (type == List.class && tag instanceof ListTag listTag) {
            List<Object> values = new ArrayList<>(listTag.size());
            for (Tag entryTag : listTag) {
                Object entry = readFromNBT(entryTag, typeClass.getParamSafe());
                if (entry != null) {
                    values.add(entry);
                }
            }
            return (T) values;
        }
        if (tag instanceof CompoundTag compoundTag) {
            if (type == ItemStack.class) {
                return (T) ItemStack.of(compoundTag);
            }
            if (type == DimensionalCoords.class) {
                ResourceLocation dimension = compoundTag.contains("dimension", Tag.TAG_STRING)
                        ? ResourceLocation.tryParse(compoundTag.getString("dimension"))
                        : null;
                if (dimension != null) {
                    return (T) new DimensionalCoords(
                            compoundTag.getInt("x"),
                            compoundTag.getInt("y"),
                            compoundTag.getInt("z"),
                            DimensionalCoords.dimension(dimension)
                    );
                }
                return (T) new DimensionalCoords(
                        compoundTag.getInt("x"),
                        compoundTag.getInt("y"),
                        compoundTag.getInt("z"),
                        compoundTag.getInt("dim")
                );
            }
        }
        return null;
    }

    public static void toBytes(ByteBuf buffer, Object obj) {
        if (obj instanceof Byte value) {
            buffer.writeByte(value);
        } else if (obj instanceof Short value) {
            buffer.writeShort(value);
        } else if (obj instanceof Integer value) {
            buffer.writeInt(value);
        } else if (obj instanceof Long value) {
            buffer.writeLong(value);
        } else if (obj instanceof Float value) {
            buffer.writeFloat(value);
        } else if (obj instanceof Double value) {
            buffer.writeDouble(value);
        } else if (obj instanceof Boolean value) {
            buffer.writeBoolean(value);
        } else {
            buffer.writeBoolean(obj != null);
            if (obj == null) {
                return;
            }

            FriendlyByteBuf friendlyBuffer = friendly(buffer);
            if (obj instanceof ISerializableObject<?> serializable) {
                serializable.toBytes(buffer);
            } else if (obj instanceof String value) {
                friendlyBuffer.writeUtf(value);
            } else if (obj instanceof List<?> values) {
                buffer.writeInt(values.size());
                values.forEach(value -> toBytes(buffer, value));
            } else if (obj instanceof ItemStack itemStack) {
                friendlyBuffer.writeItem(itemStack);
            } else if (obj instanceof DimensionalCoords coords) {
                buffer.writeInt(coords.posX);
                buffer.writeInt(coords.posY);
                buffer.writeInt(coords.posZ);
                friendlyBuffer.writeResourceLocation(coords.dimensionLocation());
            }
        }
    }

    public static <T> T fromBytes(ByteBuf buffer, Class<T> type) {
        return fromBytes(buffer, new ClassType<>(type));
    }

    @SuppressWarnings("unchecked")
    public static <T> T fromBytes(ByteBuf buffer, ClassType<T> typeClass) {
        Class<T> type = typeClass.getType();
        if (type == Byte.class) return (T) Byte.valueOf(buffer.readByte());
        if (type == Short.class) return (T) Short.valueOf(buffer.readShort());
        if (type == Integer.class) return (T) Integer.valueOf(buffer.readInt());
        if (type == Long.class) return (T) Long.valueOf(buffer.readLong());
        if (type == Float.class) return (T) Float.valueOf(buffer.readFloat());
        if (type == Double.class) return (T) Double.valueOf(buffer.readDouble());
        if (type == Boolean.class) return (T) Boolean.valueOf(buffer.readBoolean());
        if (!buffer.readBoolean()) {
            return null;
        }

        FriendlyByteBuf friendlyBuffer = friendly(buffer);
        if (ISerializableObject.class.isAssignableFrom(type)) {
            ISaveAdapter<?> adapter = ADAPTERS.get(type);
            return adapter == null ? null : (T) adapter.fromBytes(buffer);
        }
        if (type == String.class) return (T) friendlyBuffer.readUtf();
        if (type == List.class) {
            int size = buffer.readInt();
            List<Object> values = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                Object entry = fromBytes(buffer, typeClass.getParamSafe());
                if (entry != null) {
                    values.add(entry);
                }
            }
            return (T) values;
        }
        if (type == ItemStack.class) return (T) friendlyBuffer.readItem();
        if (type == DimensionalCoords.class) {
            return (T) new DimensionalCoords(
                    buffer.readInt(),
                    buffer.readInt(),
                    buffer.readInt(),
                    DimensionalCoords.dimension(friendlyBuffer.readResourceLocation())
            );
        }
        return null;
    }

    public static <T extends FiskRegistryEntry<T>> List<T> readNBTList(
            CompoundTag compound,
            String name,
            FiskSimpleRegistry<T> registry
    ) {
        if (!compound.contains(name, Tag.TAG_LIST)) {
            return null;
        }

        ListTag tagList = compound.getList(name, Tag.TAG_STRING);
        List<T> values = new ArrayList<>(tagList.size());
        for (int i = 0; i < tagList.size(); i++) {
            T entry = registry.getObject(tagList.getString(i));
            if (entry != null) {
                values.add(entry);
            }
        }
        return values;
    }

    public static CompoundTag getCompound(String value) {
        try {
            return TagParser.parseTag(value);
        } catch (CommandSyntaxException ignored) {
            return new CompoundTag();
        }
    }

    public static <T extends ISerializableObject<T>> void registerAdapter(
            Class<? extends T> type,
            Class<? extends ISaveAdapter<T>> adapter
    ) {
        try {
            ADAPTERS.put(type, adapter.getDeclaredConstructor().newInstance());
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Failed to register save adapter for " + type.getName(), exception);
        }
    }

    private static FriendlyByteBuf friendly(ByteBuf buffer) {
        return buffer instanceof FriendlyByteBuf friendly ? friendly : new FriendlyByteBuf(buffer);
    }

    public interface ISerializableObject<T extends ISerializableObject<T>> {
        Tag writeToNBT();

        void toBytes(ByteBuf buffer);
    }

    public interface ISaveAdapter<T extends ISerializableObject<T>> {
        T readFromNBT(Tag tag);

        T fromBytes(ByteBuf buffer);
    }
}
