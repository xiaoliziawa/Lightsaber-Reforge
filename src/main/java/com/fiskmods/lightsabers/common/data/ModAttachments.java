package com.fiskmods.lightsabers.common.data;

import com.fiskmods.lightsabers.Lightsabers;
import com.mojang.serialization.MapCodec;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public final class ModAttachments {
    private static final MapCodec<CompoundTag> COMPOUND_CODEC =
            MapCodec.assumeMapUnsafe(CompoundTag.CODEC);
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, Lightsabers.MODID);

    public static final Supplier<AttachmentType<ALEntityData>> LIVING_DATA =
            ATTACHMENTS.register(
                    "living_data",
                    () -> AttachmentType.builder(ALEntityData::new)
                            .serialize(serializer(ALEntityData::new))
                            .build()
            );
    public static final Supplier<AttachmentType<ALPlayerData>> PLAYER_DATA =
            ATTACHMENTS.register(
                    "player_data",
                    () -> AttachmentType.builder(ALPlayerData::new)
                            .serialize(serializer(ALPlayerData::new))
                            .copyOnDeath()
                            .build()
            );

    private ModAttachments() {
    }

    public static void register(IEventBus modEventBus) {
        ATTACHMENTS.register(modEventBus);
    }

    private static <T extends PersistentData> IAttachmentSerializer<T> serializer(
            Supplier<T> factory
    ) {
        return new IAttachmentSerializer<>() {
            @Override
            public T read(
                    IAttachmentHolder holder,
                    ValueInput input
            ) {
                T data = factory.get();
                data.load(input.read(COMPOUND_CODEC).orElseGet(CompoundTag::new));
                return data;
            }

            @Override
            public boolean write(T attachment, ValueOutput output) {
                output.store(COMPOUND_CODEC, attachment.save());
                return true;
            }
        };
    }

    interface PersistentData {
        CompoundTag save();

        void load(CompoundTag tag);
    }
}
