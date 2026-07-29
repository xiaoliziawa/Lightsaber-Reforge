package com.fiskmods.lightsabers.common.hilt;

import com.fiskmods.lightsabers.common.lightsaber.CrystalColor;
import com.fiskmods.lightsabers.common.lightsaber.PartType;

public class HiltSpear extends Hilt {
    private static final float PIXELS_PER_MODEL_UNIT = 9.0F;
    private static final float EMITTER_MODEL_EXTENT = 1.2437F;
    private static final float SWITCH_MODEL_EXTENT = 0.3125F;
    private static final float BODY_MODEL_EXTENT = 3.1188F;
    private static final float POMMEL_MODEL_EXTENT = 0.5F;

    private final Part[] parts = makeParts();

    private Part[] makeParts() {
        Part[] result = new Part[PartType.values().length];
        result[PartType.EMITTER.ordinal()] = part(PartType.EMITTER, EMITTER_MODEL_EXTENT);
        result[PartType.SWITCH_SECTION.ordinal()] = part(PartType.SWITCH_SECTION, SWITCH_MODEL_EXTENT);
        result[PartType.BODY.ordinal()] = part(PartType.BODY, BODY_MODEL_EXTENT);
        result[PartType.POMMEL.ordinal()] = part(PartType.POMMEL, POMMEL_MODEL_EXTENT);
        return result;
    }

    private static Part part(PartType type, float modelExtent) {
        return new Part(type, modelExtent * PIXELS_PER_MODEL_UNIT);
    }

    @Override
    public CrystalColor getColor() {
        return CrystalColor.RED;
    }

    @Override
    public boolean supportsDoubleLightsaber() {
        return false;
    }

    @Override
    public boolean requiresUniformAssembly() {
        return true;
    }

    @Override
    public Part[] getParts() {
        return parts;
    }
}
