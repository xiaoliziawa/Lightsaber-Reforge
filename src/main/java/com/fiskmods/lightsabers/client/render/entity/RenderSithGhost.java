package com.fiskmods.lightsabers.client.render.entity;

import com.fiskmods.lightsabers.Lightsabers;
import com.fiskmods.lightsabers.client.model.ModelSithGhost;
import com.fiskmods.lightsabers.common.entity.EntitySithGhost;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.resources.Identifier;

public class RenderSithGhost
        extends HumanoidMobRenderer<
                EntitySithGhost,
                HumanoidRenderState,
                ModelSithGhost
        > {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(
            Lightsabers.MODID,
            "textures/models/sith_ghost.png"
    );

    public RenderSithGhost(EntityRendererProvider.Context context) {
        super(context, new ModelSithGhost(context.bakeLayer(ModelSithGhost.LAYER)), 0.5F);
    }

    @Override
    public Identifier getTextureLocation(HumanoidRenderState state) {
        return TEXTURE;
    }

    @Override
    public HumanoidRenderState createRenderState() {
        return new HumanoidRenderState();
    }
}
