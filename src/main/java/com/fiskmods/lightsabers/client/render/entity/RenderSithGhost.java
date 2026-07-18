package com.fiskmods.lightsabers.client.render.entity;

import com.fiskmods.lightsabers.Lightsabers;
import com.fiskmods.lightsabers.client.model.ModelSithGhost;
import com.fiskmods.lightsabers.common.entity.EntitySithGhost;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;

public class RenderSithGhost
        extends HumanoidMobRenderer<EntitySithGhost, ModelSithGhost> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            Lightsabers.MODID,
            "textures/models/sith_ghost.png"
    );

    public RenderSithGhost(EntityRendererProvider.Context context) {
        super(context, new ModelSithGhost(context.bakeLayer(ModelSithGhost.LAYER)), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(EntitySithGhost entity) {
        return TEXTURE;
    }
}
