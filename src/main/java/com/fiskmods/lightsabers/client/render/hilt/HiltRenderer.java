package com.fiskmods.lightsabers.client.render.hilt;

import com.fiskmods.lightsabers.Lightsabers;
import com.fiskmods.lightsabers.client.model.legacy.LegacyModelBase;
import com.fiskmods.lightsabers.common.hilt.Hilt;
import com.fiskmods.lightsabers.common.lightsaber.PartType;

import fiskfille.utils.registry.FiskRegistryEntry;
import fiskfille.utils.registry.FiskSimpleRegistry;
import net.minecraft.resources.Identifier;

public abstract class HiltRenderer extends FiskRegistryEntry<HiltRenderer>
{
    public static final FiskSimpleRegistry<HiltRenderer> REGISTRY = new FiskSimpleRegistry(Lightsabers.MODID, "graflex");
    
    public static void register(String key, HiltRenderer value)
    {
        REGISTRY.putObject(key, value);
    }
    
    public static void register(Hilt key, HiltRenderer value)
    {
        register(key.delegate.name(), value);
    }
    
    public static HiltRenderer get(String key)
    {
        return REGISTRY.getObject(key);
    }
    
    public static HiltRenderer get(Hilt key)
    {
        return key == null ? null : get(key.delegate.name());
    }
    
    public abstract LegacyModelBase getEmitter();
    
    public abstract LegacyModelBase getSwitchSection();
    
    public abstract LegacyModelBase getBody();
    
    public abstract LegacyModelBase getPommel();
    
    public LegacyModelBase getModel(PartType type)
    {
        switch (type)
        {
        case EMITTER:
            return getEmitter();
        case SWITCH_SECTION:
            return getSwitchSection();
        case BODY:
            return getBody();
        default:
            return getPommel();
        }
    }
    
    public Identifier getTexture(PartType type)
    {
        return Identifier.fromNamespaceAndPath(
                getDomain(),
                String.format(
                        "textures/models/lightsaber/%s_%s.png",
                        type.textureName,
                        getRegistryName().getPath()
                )
        );
    }
    
    public final Hilt getHilt()
    {
        return Hilt.REGISTRY.getObject(delegate.name());
    }
}
