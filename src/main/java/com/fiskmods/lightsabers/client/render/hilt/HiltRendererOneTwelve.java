package com.fiskmods.lightsabers.client.render.hilt;

import com.fiskmods.lightsabers.common.lightsaber.PartType;
import com.fiskmods.lightsabers.client.model.legacy.LegacyModelBase;

import net.minecraft.resources.Identifier;

public class HiltRendererOneTwelve extends HiltRendererBase
{
    public HiltRendererOneTwelve(
            LegacyModelBase emitter,
            LegacyModelBase switchSection,
            LegacyModelBase body,
            LegacyModelBase pommel
    )
    {
        super(emitter, switchSection, body, pommel);
    }
    
    @Override
    public Identifier getTexture(PartType type)
    {
        return Identifier.fromNamespaceAndPath(
                getDomain(),
                String.format(
                        "textures/models/lightsaber/%s.png",
                        getRegistryName().getPath()
                )
        );
    }
}
