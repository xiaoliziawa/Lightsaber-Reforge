package com.fiskmods.lightsabers.client.command;

import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexSorting;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.bus.api.SubscribeEvent;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;

import java.io.File;
import java.util.Arrays;

public final class CommandExportIcon {
    private static final String SIZE = "size";
    private static final int DEFAULT_SIZE = 1080;
    private static final int MIN_SIZE = 1024;
    private static final int MAX_SIZE = 4096;
    private static final int[] SUGGESTED_SIZES = {1024, 1080, 1440, 2048, 2160, 3840, 4096};

    private static final String OUTPUT_DIR = "icon_exports";
    private static final float GUI_NEAR_PLANE = 1000.0F;
    private static final float GUI_FAR_PLANE = 11000.0F;
    private static final float GUI_ICON_UNITS = 16.0F;

    @SubscribeEvent
    public void onRegisterClientCommands(RegisterClientCommandsEvent event) {
        register(event.getDispatcher());
    }

    private static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("exporticon")
                .executes(context -> export(context, DEFAULT_SIZE))
                .then(Commands.argument(SIZE, IntegerArgumentType.integer(MIN_SIZE, MAX_SIZE))
                        .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                                Arrays.stream(SUGGESTED_SIZES).mapToObj(Integer::toString),
                                builder
                        ))
                        .executes(context -> export(
                                context,
                                IntegerArgumentType.getInteger(context, SIZE)
                        ))));
    }

    private static int export(CommandContext<CommandSourceStack> context, int size) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null) {
            return 0;
        }

        ItemStack stack = player.getMainHandItem();
        if (stack.isEmpty()) {
            stack = player.getOffhandItem();
        }
        if (stack.isEmpty()) {
            context.getSource().sendFailure(Component.translatable("commands.exporticon.empty"));
            return 0;
        }

        try {
            File file = renderToFile(minecraft, stack, size);
            Component link = Component.literal(file.getName())
                    .withStyle(ChatFormatting.UNDERLINE)
                    .withStyle(style -> style.withClickEvent(
                            new ClickEvent(ClickEvent.Action.OPEN_FILE, file.getAbsolutePath())
                    ));
            Component name = stack.getHoverName();
            context.getSource().sendSuccess(
                    () -> Component.translatable("commands.exporticon.success", name, size, size, link),
                    false
            );
            return 1;
        } catch (Exception exception) {
            context.getSource().sendFailure(Component.translatable(
                    "commands.exporticon.failure",
                    exception.getMessage()
            ));
            return 0;
        }
    }

    private static File renderToFile(Minecraft minecraft, ItemStack stack, int size) {
        TextureTarget target = new TextureTarget(size, size, true, Minecraft.ON_OSX);
        NativeImage image = new NativeImage(NativeImage.Format.RGBA, size, size, false);

        Matrix4f previousProjection = RenderSystem.getProjectionMatrix();
        VertexSorting previousSorting = RenderSystem.getVertexSorting();
        Matrix4fStack modelView = RenderSystem.getModelViewStack();

        try {
            target.setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
            target.clear(Minecraft.ON_OSX);
            target.bindWrite(true);

            Matrix4f projection = new Matrix4f().setOrtho(
                    0.0F, GUI_ICON_UNITS, GUI_ICON_UNITS, 0.0F, GUI_NEAR_PLANE, GUI_FAR_PLANE
            );
            RenderSystem.setProjectionMatrix(projection, VertexSorting.ORTHOGRAPHIC_Z);

            modelView.pushMatrix();
            modelView.identity();
            modelView.translate(0.0F, 0.0F, GUI_NEAR_PLANE - GUI_FAR_PLANE);
            RenderSystem.applyModelViewMatrix();
            Lighting.setupFor3DItems();

            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(
                    GlStateManager.SourceFactor.SRC_ALPHA,
                    GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                    GlStateManager.SourceFactor.ONE,
                    GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA
            );

            GuiGraphics graphics = new GuiGraphics(minecraft, minecraft.renderBuffers().bufferSource());
            graphics.renderItem(stack, 0, 0);
            graphics.flush();

            RenderSystem.defaultBlendFunc();

            RenderSystem.bindTexture(target.getColorTextureId());
            image.downloadTexture(0, false);
            image.flipY();
        } finally {
            modelView.popMatrix();
            RenderSystem.applyModelViewMatrix();
            RenderSystem.setProjectionMatrix(previousProjection, previousSorting);
            target.unbindWrite();
            target.destroyBuffers();
            minecraft.getMainRenderTarget().bindWrite(true);
        }

        File directory = new File(minecraft.gameDirectory, OUTPUT_DIR);
        directory.mkdirs();
        File file = new File(directory, fileName(stack));
        Util.ioPool().execute(() -> {
            try {
                image.writeToFile(file);
            } catch (Exception exception) {
                Minecraft.getInstance().execute(() -> {
                    LocalPlayer player = minecraft.player;
                    if (player != null) {
                        player.sendSystemMessage(Component.translatable(
                                "commands.exporticon.failure",
                                exception.getMessage()
                        ));
                    }
                });
            } finally {
                image.close();
            }
        });
        return file;
    }

    private static String fileName(ItemStack stack) {
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        String base = id == null ? "item" : id.getNamespace() + "_" + id.getPath();
        return base + "_" + Util.getFilenameFormattedDateTime() + ".png";
    }
}
