package com.fiskmods.lightsabers.client.render.item;

import com.fiskmods.lightsabers.client.model.tile.ModelCrystal;
import com.fiskmods.lightsabers.client.render.CrystalRenderHelper;
import com.fiskmods.lightsabers.common.item.ItemCrystal;
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

public final class CrystalItemRenderer implements SpecialModelRenderer<
        CrystalItemRenderer.RenderArgument
> {
    private static final float ITEM_ALPHA = 0.6F;
    private static final float GUI_SCALE = 1.8F;
    private static final float GUI_VERTICAL_OFFSET = 0.18F;
    private static final float FIRST_PERSON_SCALE = 1.8F;
    private static final float THIRD_PERSON_SCALE = 1.5F;
    private static final float GROUND_SCALE = 1.5F;
    private static final float FIXED_SCALE = 2.0F;
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

    private final ModelCrystal model;

    private CrystalItemRenderer(ModelCrystal model) {
        this.model = model;
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
        poseStack.pushPose();
        applyDisplayTransform(argument.displayContext(), poseStack);
        poseStack.translate(0.5F, 1.5F, 0.5F);
        poseStack.scale(1.0F, -1.0F, -1.0F);
        CrystalRenderHelper.render(
                model,
                poseStack,
                collector,
                ItemCrystal.get(argument.stack()),
                ITEM_ALPHA
        );
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
        switch (displayContext) {
            case GUI -> {
                beginTransform(poseStack, GUI_VERTICAL_OFFSET);
                poseStack.mulPose(Axis.XP.rotationDegrees(20.0F));
                poseStack.mulPose(Axis.YP.rotationDegrees(225.0F));
                finishTransform(poseStack, GUI_SCALE);
            }
            case FIRST_PERSON_LEFT_HAND -> {
                beginTransform(poseStack, 0.0F);
                poseStack.mulPose(Axis.YP.rotationDegrees(35.0F));
                poseStack.mulPose(Axis.ZP.rotationDegrees(-20.0F));
                finishTransform(poseStack, FIRST_PERSON_SCALE);
            }
            case FIRST_PERSON_RIGHT_HAND -> {
                beginTransform(poseStack, 0.0F);
                poseStack.mulPose(Axis.YP.rotationDegrees(-35.0F));
                poseStack.mulPose(Axis.ZP.rotationDegrees(20.0F));
                finishTransform(poseStack, FIRST_PERSON_SCALE);
            }
            case THIRD_PERSON_LEFT_HAND -> {
                beginTransform(poseStack, 0.1F);
                poseStack.mulPose(Axis.ZP.rotationDegrees(-20.0F));
                finishTransform(poseStack, THIRD_PERSON_SCALE);
            }
            case THIRD_PERSON_RIGHT_HAND -> {
                beginTransform(poseStack, 0.1F);
                poseStack.mulPose(Axis.ZP.rotationDegrees(20.0F));
                finishTransform(poseStack, THIRD_PERSON_SCALE);
            }
            case GROUND -> centerAndScale(poseStack, GROUND_SCALE, 0.0F);
            case FIXED -> {
                beginTransform(poseStack, 0.04F);
                poseStack.mulPose(Axis.XP.rotationDegrees(20.0F));
                poseStack.mulPose(Axis.YP.rotationDegrees(45.0F));
                finishTransform(poseStack, FIXED_SCALE);
            }
            default -> centerAndScale(poseStack, FIXED_SCALE, 0.0F);
        }
    }

    private static void beginTransform(PoseStack poseStack, float verticalOffset) {
        poseStack.translate(0.5F, verticalOffset, 0.5F);
    }

    private static void finishTransform(PoseStack poseStack, float scale) {
        poseStack.scale(scale, scale, scale);
        poseStack.translate(-0.5F, 0.0F, -0.5F);
    }

    private static void centerAndScale(
            PoseStack poseStack,
            float scale,
            float verticalOffset
    ) {
        beginTransform(poseStack, verticalOffset);
        finishTransform(poseStack, scale);
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
            CrystalItemRenderer renderer = new CrystalItemRenderer(new ModelCrystal(
                    context.entityModelSet().bakeLayer(ModelCrystal.LAYER)
            ));
            return new BakedItemModel(renderer, new Matrix4f(transformation));
        }

        @Override
        public MapCodec<Unbaked> type() {
            return MAP_CODEC;
        }
    }

    private record BakedItemModel(
            CrystalItemRenderer renderer,
            Matrix4fc transformation
    ) implements ItemModel {
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
                    renderer,
                    new RenderArgument(item.copy(), displayContext)
            );
        }
    }
}
