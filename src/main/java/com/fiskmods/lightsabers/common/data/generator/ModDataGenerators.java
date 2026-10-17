package com.fiskmods.lightsabers.common.data.generator;

import net.neoforged.neoforge.data.event.GatherDataEvent;

public final class ModDataGenerators {
    private ModDataGenerators() {
    }

    public static void gatherData(GatherDataEvent.Client event) {
        event.createProvider(WorldgenDataProvider::new);
    }
}
