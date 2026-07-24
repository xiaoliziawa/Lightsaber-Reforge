package com.fiskmods.lightsabers.common.data.generator;

import net.minecraft.data.DataGenerator;
import net.neoforged.neoforge.data.event.GatherDataEvent;

public final class ModDataGenerators {
    private ModDataGenerators() {
    }

    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        generator.addProvider(
                event.includeServer(),
                new WorldgenDataProvider(generator.getPackOutput())
        );
    }
}
