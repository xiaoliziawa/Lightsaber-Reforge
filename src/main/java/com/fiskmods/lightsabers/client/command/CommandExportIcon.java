package com.fiskmods.lightsabers.client.command;

import com.fiskmods.lightsabers.mixin.GameRendererAccessor;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import com.mojang.renderpearl.api.commands.CommandEncoder;
import com.mojang.renderpearl.api.textures.GpuTexture;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.util.Util;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.state.WindowRenderState;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.bus.api.SubscribeEvent;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public final class CommandExportIcon {
    private static final String SIZE = "size";
    private static final int DEFAULT_SIZE = 1080;
    private static final int MIN_SIZE = 1024;
    private static final int MAX_SIZE = 4096;
    private static final int[] SUGGESTED_SIZES = {1024, 1080, 1440, 2048, 2160, 3840, 4096};

    private static final String OUTPUT_DIR = "icon_exports";
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
                            new ClickEvent.OpenFile(file)
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
        TextureTarget target = new TextureTarget(
                "Lightsabers icon export",
                size,
                size,
                GpuFormat.RGBA8_UNORM,
                GpuFormat.D32_FLOAT
        );
        boolean readbackScheduled = false;

        try {
            WindowRenderState windowState = minecraft.gameRenderer
                    .gameRenderState()
                    .windowRenderState;
            int previousWidth = windowState.width;
            int previousHeight = windowState.height;
            int previousGuiScale = windowState.guiScale;
            RenderTarget previousTarget = minecraft.gameRenderer.mainRenderTarget();
            GameRendererAccessor accessor = (GameRendererAccessor) minecraft.gameRenderer;

            RenderSystem.backupProjectionMatrix();
            try {
                accessor.lightsabers$setMainRenderTarget(target);
                int guiScale = Math.max(1, size / (int) GUI_ICON_UNITS);
                windowState.width = size;
                windowState.height = size;
                windowState.guiScale = guiScale;
                clearTarget(target);

                GuiRenderState guiRenderState = new GuiRenderState();
                GuiGraphicsExtractor graphics = new GuiGraphicsExtractor(
                        minecraft,
                        guiRenderState,
                        0,
                        0
                );
                float itemScale = size / (GUI_ICON_UNITS * guiScale);
                graphics.pose().scale(itemScale, itemScale);
                graphics.item(stack, 0, 0);

                try (GuiRenderer guiRenderer = new GuiRenderer(
                                guiRenderState,
                                minecraft.gameRenderer.featureRenderDispatcher(),
                                List.of()
                        )) {
                    guiRenderer.render();
                    guiRenderer.endFrame();
                }
            } finally {
                accessor.lightsabers$setMainRenderTarget(previousTarget);
                windowState.width = previousWidth;
                windowState.height = previousHeight;
                windowState.guiScale = previousGuiScale;
                RenderSystem.restoreProjectionMatrix();
            }

            File directory = new File(minecraft.gameDirectory, OUTPUT_DIR);
            directory.mkdirs();
            File file = new File(directory, fileName(stack));
            saveTarget(minecraft, target, file);
            readbackScheduled = true;
            return file;
        } finally {
            if (!readbackScheduled) {
                target.destroyBuffers();
            }
        }
    }

    private static void clearTarget(TextureTarget target) {
        RenderSystem.getDevice().createCommandEncoder().clearColorAndDepthTextures(
                Objects.requireNonNull(target.getColorTexture()),
                GuiRenderer.CLEAR_COLOR,
                Objects.requireNonNull(target.getDepthTexture()),
                RenderSystem.DEFAULT_DEPTH_CLEAR_VALUE
        );
    }

    private static void saveTarget(
            Minecraft minecraft,
            TextureTarget target,
            File file
    ) {
        int width = target.width;
        int height = target.height;
        GpuTexture sourceTexture = Objects.requireNonNull(target.getColorTexture());
        GpuBuffer buffer = RenderSystem.getDevice().createBuffer(
                () -> "Lightsabers icon export readback",
                GpuBuffer.USAGE_MAP_READ | GpuBuffer.USAGE_COPY_DST,
                (long) width * height * sourceTexture.getFormat().blockSize()
        );
        boolean submitted = false;
        try {
            CommandEncoder commandEncoder = RenderSystem.getDevice().createCommandEncoder();
            commandEncoder.copyTextureToBuffer(
                    sourceTexture,
                    buffer,
                    0L,
                    () -> finishReadback(
                            minecraft,
                            target,
                            buffer,
                            width,
                            height,
                            file
                    ),
                    0
            );
            submitted = true;
        } finally {
            if (!submitted) {
                buffer.close();
            }
        }
    }

    private static void finishReadback(
            Minecraft minecraft,
            TextureTarget target,
            GpuBuffer buffer,
            int width,
            int height,
            File file
    ) {
        NativeImage image = new NativeImage(width, height, false);
        boolean writeScheduled = false;
        try {
            try (GpuBufferSlice.MappedView read = buffer.map(true, false)) {
                int pixelSize = Objects.requireNonNull(target.getColorTexture())
                        .getFormat()
                        .blockSize();
                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++) {
                        int color = read.data().getInt((x + y * width) * pixelSize);
                        image.setPixelABGR(x, height - y - 1, color);
                    }
                }
            }
            Util.ioPool().execute(() -> writeImage(minecraft, image, file));
            writeScheduled = true;
        } finally {
            buffer.close();
            target.destroyBuffers();
            if (!writeScheduled) {
                image.close();
            }
        }
    }

    private static void writeImage(
            Minecraft minecraft,
            NativeImage image,
            File file
    ) {
        try (image) {
            image.writeToFile(file);
        } catch (Exception exception) {
            minecraft.execute(() -> {
                LocalPlayer player = minecraft.player;
                if (player != null) {
                    player.sendSystemMessage(Component.translatable(
                            "commands.exporticon.failure",
                            exception.getMessage()
                    ));
                }
            });
        }
    }

    private static String fileName(ItemStack stack) {
        Identifier id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        String base = id == null ? "item" : id.getNamespace() + "_" + id.getPath();
        return base + "_" + Util.getFilenameFormattedDateTime() + ".png";
    }
}
