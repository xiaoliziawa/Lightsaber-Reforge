package com.fiskmods.lightsabers.client.render.item;

import com.fiskmods.lightsabers.client.render.HolocronObjRenderer;
import com.fiskmods.lightsabers.common.block.HolocronType;
import com.fiskmods.lightsabers.common.item.ItemHolocron;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mojang.serialization.MapCodec;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

public final class HolocronItemRenderer implements SpecialModelRenderer<
        HolocronItemRenderer.RenderArgument
> {
    private static final HolocronItemRenderer INSTANCE =
            new HolocronItemRenderer();
    private static final float GUI_SCALE = 0.9F;
    private static final float GROUND_SCALE = 1.0F;
    private static final float FIXED_SCALE = 1.25F;
    private static final Vector3fc[] EXTENTS = {
            new Vector3f(-1.0F, -1.0F, -1.0F),
            new Vector3f(-1.0F, -1.0F, 1.0F),
            new Vector3f(-1.0F, 1.0F, -1.0F),
            new Vector3f(-1.0F, 1.0F, 1.0F),
            new Vector3f(1.0F, -1.0F, -1.0F),
            new Vector3f(1.0F, -1.0F, 1.0F),
            new Vector3f(1.0F, 1.0F, -1.0F),
            new Vector3f(1.0F, 1.0F, 1.0F)
    };
    private static final Supplier<Vector3fc[]> EXTENTS_SUPPLIER = () -> EXTENTS;

    private HolocronItemRenderer() {
    }

    @Override
    public void submit(
            @Nullable RenderArgument argument,
            PoseStack poseStack,
            SubmitNodeCollector collector,
            int packedLight,
            int packedOverlay,
            boolean hasFoil,
            int outlineColor
    ) {
        if (argument == null) {
            return;
        }
        ItemStack stack = argument.stack();
        ItemDisplayContext displayContext = argument.displayContext();
        poseStack.pushPose();
        HolocronType type = stack.getItem() instanceof ItemHolocron holocron
                ? holocron.getType()
                : HolocronType.SITH;
        if (displayContext == ItemDisplayContext.GUI) {
            poseStack.translate(0.5F, 0.5F, 0.5F);
            poseStack.scale(GUI_SCALE, GUI_SCALE, GUI_SCALE);
            HolocronObjRenderer.renderItemIcon(
                    type,
                    poseStack,
                    collector,
                    packedOverlay
            );
        } else {
            applyDisplayTransform(displayContext, poseStack);
            HolocronObjRenderer.renderModel(
                    type,
                    0.0F,
                    0.0F,
                    poseStack,
                    collector,
                    packedOverlay
            );
        }
        poseStack.popPose();
    }

    @Override
    public void getExtents(Consumer<Vector3fc> output) {
        for (Vector3fc extent : EXTENTS) {
            output.accept(extent);
        }
    }

    @Override
    public RenderArgument extractArgument(ItemStack stack) {
        return new RenderArgument(stack.copy(), ItemDisplayContext.NONE);
    }

    private static void applyDisplayTransform(
            ItemDisplayContext displayContext,
            PoseStack poseStack
    ) {
        poseStack.translate(0.5F, 0.5F, 0.5F);
        switch (displayContext) {
            case FIRST_PERSON_LEFT_HAND,
                    FIRST_PERSON_RIGHT_HAND,
                    THIRD_PERSON_LEFT_HAND,
                    THIRD_PERSON_RIGHT_HAND -> {
            }
            case GROUND -> poseStack.scale(
                    GROUND_SCALE,
                    GROUND_SCALE,
                    GROUND_SCALE
            );
            case FIXED -> {
                poseStack.mulPose(Axis.XP.rotationDegrees(20.0F));
                poseStack.mulPose(Axis.YP.rotationDegrees(45.0F));
                poseStack.scale(FIXED_SCALE, FIXED_SCALE, FIXED_SCALE);
            }
            default -> poseStack.scale(
                    FIXED_SCALE,
                    FIXED_SCALE,
                    FIXED_SCALE
            );
        }
    }

    public record RenderArgument(
            ItemStack stack,
            ItemDisplayContext displayContext
    ) {
    }

    public record Unbaked() implements ItemModel.Unbaked {
        public static final MapCodec<Unbaked> MAP_CODEC =
                MapCodec.unit(new Unbaked());

        @Override
        public void resolveDependencies(Resolver resolver) {
        }

        @Override
        public ItemModel bake(ItemModel.BakingContext context, Matrix4fc transformation) {
            return new BakedItemModel(new Matrix4f(transformation));
        }

        @Override
        public MapCodec<Unbaked> type() {
            return MAP_CODEC;
        }
    }

    private record BakedItemModel(Matrix4fc transformation) implements ItemModel {
        @Override
        public void update(
                ItemStackRenderState output,
                ItemStack item,
                ItemModelResolver resolver,
                ItemDisplayContext displayContext,
                @Nullable ClientLevel level,
                @Nullable ItemOwner owner,
                int seed
        ) {
            output.appendModelIdentityElement(this);
            output.appendModelIdentityElement(ItemRenderIdentity.of(
                    item,
                    displayContext
            ));
            ItemStackRenderState.LayerRenderState layer = output.newLayer();
            layer.setLocalTransform(transformation);
            layer.setExtents(ItemRenderExtents.forDisplayContext(
                    displayContext,
                    EXTENTS_SUPPLIER
            ));
            layer.setupSpecialModel(
                    INSTANCE,
                    new RenderArgument(item.copy(), displayContext)
            );
        }
    }
}
