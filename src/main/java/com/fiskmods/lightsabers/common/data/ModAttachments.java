package com.fiskmods.lightsabers.common.data;

import com.fiskmods.lightsabers.Lightsabers;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public final class ModAttachments {
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

    private static <T extends PersistentData> IAttachmentSerializer<CompoundTag, T> serializer(
            Supplier<T> factory
    ) {
        return new IAttachmentSerializer<>() {
            @Override
            public T read(
                    IAttachmentHolder holder,
                    CompoundTag tag,
                    HolderLookup.Provider provider
            ) {
                T data = factory.get();
                data.load(tag);
                return data;
            }

            @Override
            public CompoundTag write(T attachment, HolderLookup.Provider provider) {
                return attachment.save();
            }
        };
    }

    interface PersistentData {
        CompoundTag save();

        void load(CompoundTag tag);
    }
}
