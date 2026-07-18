package com.fiskmods.lightsabers.common.hilt;

import com.fiskmods.lightsabers.common.lightsaber.CrystalColor;
import com.fiskmods.lightsabers.common.lightsaber.PartType;

public class HiltSpinning extends Hilt {
    private final Part[] parts = makeParts();

    private Part[] makeParts() {
        Part[] result = new Part[PartType.values().length];
        result[PartType.EMITTER.ordinal()] = new Part(PartType.EMITTER, 5.0F);
        result[PartType.SWITCH_SECTION.ordinal()] = new Part(PartType.SWITCH_SECTION, 4.0F);
        result[PartType.BODY.ordinal()] = new Part(PartType.BODY, 24.0F);
        result[PartType.POMMEL.ordinal()] = new Part(PartType.POMMEL, 5.0F);
        return result;
    }

    @Override
    public CrystalColor getColor() {
        return CrystalColor.DEEP_BLUE;
    }

    @Override
    public boolean supportsDoubleLightsaber() {
        return false;
    }

    @Override
    public Part[] getParts() {
        return parts;
    }
}
