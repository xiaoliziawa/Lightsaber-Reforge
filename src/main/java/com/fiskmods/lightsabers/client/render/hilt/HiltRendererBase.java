package com.fiskmods.lightsabers.client.render.hilt;

import com.fiskmods.lightsabers.client.model.legacy.LegacyModelBase;

public class HiltRendererBase extends HiltRenderer
{
    public final LegacyModelBase emitter;
    public final LegacyModelBase switchSection;
    public final LegacyModelBase body;
    public final LegacyModelBase pommel;

    public HiltRendererBase(
            LegacyModelBase emitter,
            LegacyModelBase switchSection,
            LegacyModelBase body,
            LegacyModelBase pommel
    )
    {
        this.emitter = emitter;
        this.switchSection = switchSection;
        this.body = body;
        this.pommel = pommel;
    }

    @Override
    public LegacyModelBase getEmitter()
    {
        return emitter;
    }

    @Override
    public LegacyModelBase getSwitchSection()
    {
        return switchSection;
    }

    @Override
    public LegacyModelBase getBody()
    {
        return body;
    }

    @Override
    public LegacyModelBase getPommel()
    {
        return pommel;
    }
}
