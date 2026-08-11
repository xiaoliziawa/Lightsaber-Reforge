package com.fiskmods.lightsabers.client.gui;

import com.fiskmods.lightsabers.Lightsabers;
import com.fiskmods.lightsabers.client.sound.ALSounds;
import com.fiskmods.lightsabers.common.container.ContainerHolocron;
import com.fiskmods.lightsabers.common.data.ALData;
import com.fiskmods.lightsabers.common.force.Power;
import com.fiskmods.lightsabers.common.force.PowerData;
import com.fiskmods.lightsabers.common.force.PowerManager;
import com.fiskmods.lightsabers.common.force.PowerStats;
import com.fiskmods.lightsabers.common.network.ALNetworkManager;
import com.fiskmods.lightsabers.common.network.MessageUnlockPower;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.component.ItemAttributeModifiers;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GuiForcePowers extends AbstractContainerScreen<ContainerHolocron> {
    private static final Identifier GUI_TEXTURE = Identifier.fromNamespaceAndPath(
            Lightsabers.MODID,
            "textures/gui/container/force_powers.png"
    );
    private static final Identifier ICONS_TEXTURE = Identifier.fromNamespaceAndPath(
            Lightsabers.MODID,
            "textures/gui/icons.png"
    );
    private static final Identifier[][] FORCESTONE_TEXTURES = {
            {
                    blockTexture("dark_forcestone"),
                    blockTexture("dark_forcestone_inscribed"),
                    blockTexture("dark_forcestone_pillar")
            },
            {
                    blockTexture("light_forcestone"),
                    blockTexture("light_forcestone_inscribed"),
                    blockTexture("light_forcestone_pillar")
            }
    };

    private static final int PANEL_WIDTH = 256;
    private static final int PANEL_HEIGHT = 202;
    private static final int TREE_X = 16;
    private static final int TREE_Y = 17;
    private static final int TREE_WIDTH = 224;
    private static final int TREE_HEIGHT = 155;
    private static final int NODE_SPACING = 24;
    private static final int NODE_SIZE = 22;
    private static final int NODE_FRAME_SIZE = 26;
    private static final int NODE_FRAME_OFFSET = 2;
    private static final int NODE_ICON_OFFSET = 3;
    private static final int BACKGROUND_TILE_SIZE = 16;
    private static final int BACKGROUND_COORDINATE_OFFSET = 288;
    private static final int CONTENT_PADDING = NODE_SPACING;
    private static final int DRAG_THRESHOLD_SQUARED = 9;
    private static final int UNLOCKED_CONNECTION_COLOR = 0xFFA0A0A0;
    private static final int AVAILABLE_CONNECTION_COLOR = 0xFF00FF00;
    private static final int LOCKED_CONNECTION_COLOR = 0xFF000000;
    private static final int STATUS_COLOR = 0x9090FF;
    private static final int TITLE_COLOR = 0xFF404040;
    private static final int POSITIVE_STATUS_COLOR = 0xFF80FF80;
    private static final int NEGATIVE_STATUS_COLOR = 0xFFD74848;
    private static final float MIN_ZOOM = 1.0F;
    private static final float MAX_ZOOM = 2.0F;
    private static final float ZOOM_STEP = 0.25F;
    private static final double INITIAL_SCROLL_X = -82.0D;
    private static final double INITIAL_SCROLL_Y = -70.0D;

    private final PowerManager powerManager;
    private final int backgroundSeed;
    private final int minPowerX;
    private final int maxPowerX;
    private final int minPowerY;
    private final int maxPowerY;

    private double scrollX = INITIAL_SCROLL_X;
    private double scrollY = INITIAL_SCROLL_Y;
    private double pressedMouseX;
    private double pressedMouseY;
    private double previousMouseX;
    private double previousMouseY;
    private float zoom = MIN_ZOOM;
    private boolean draggingTree;
    private boolean dragMoved;
    private Power pressedPower;

    public GuiForcePowers(
            ContainerHolocron menu,
            Inventory playerInventory,
            Component title
    ) {
        super(menu, playerInventory, title, PANEL_WIDTH, PANEL_HEIGHT);
        powerManager = new PowerManager(playerInventory.player);
        backgroundSeed = playerInventory.player.getUUID().hashCode();

        int minimumX = Integer.MAX_VALUE;
        int maximumX = Integer.MIN_VALUE;
        int minimumY = Integer.MAX_VALUE;
        int maximumY = Integer.MIN_VALUE;
        for (Power power : Power.POWERS) {
            int x = getPowerWorldX(power);
            int y = getPowerWorldY(power);
            minimumX = Math.min(minimumX, x);
            maximumX = Math.max(maximumX, x + NODE_SIZE);
            minimumY = Math.min(minimumY, y);
            maximumY = Math.max(maximumY, y + NODE_SIZE);
        }
        minPowerX = minimumX;
        maxPowerX = maximumX;
        minPowerY = minimumY;
        maxPowerY = maximumY;
        clampScroll();
    }

    @Override
    protected void init() {
        super.init();
        addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> onClose())
                .bounds(leftPos + 152, topPos + 175, 80, 20)
                .build());
    }

    @Override
    public void extractRenderState(
            GuiGraphicsExtractor guiGraphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
        super.extractRenderState(guiGraphics, mouseX, mouseY, partialTick);
        renderPowerTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    public void extractBackground(
            GuiGraphicsExtractor guiGraphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
        super.extractBackground(guiGraphics, mouseX, mouseY, partialTick);
        int treeLeft = leftPos + TREE_X;
        int treeTop = topPos + TREE_Y;
        guiGraphics.enableScissor(
                treeLeft,
                treeTop,
                treeLeft + TREE_WIDTH,
                treeTop + TREE_HEIGHT
        );
        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().translate(treeLeft, treeTop);
        guiGraphics.pose().scale(1.0F / zoom, 1.0F / zoom);
        renderForcestoneBackground(guiGraphics);
        renderConnections(guiGraphics);
        renderPowerNodes(guiGraphics);
        guiGraphics.pose().popMatrix();
        guiGraphics.disableScissor();

        guiGraphics.blit(
                RenderPipelines.GUI_TEXTURED,
                GUI_TEXTURE,
                leftPos,
                topPos,
                0,
                0,
                PANEL_WIDTH,
                PANEL_HEIGHT,
                256,
                256
        );
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
        guiGraphics.text(font, title, 15, 5, TITLE_COLOR, false);
        if (minecraft == null || minecraft.player == null) {
            return;
        }

        Player player = minecraft.player;
        float forceXp = ALData.FORCE_XP.get(player);
        byte basePower = ALData.BASE_POWER.get(player);
        guiGraphics.text(
                font,
                Component.translatable("gui.forcePowers.xp", Mth.floor(forceXp)),
                15,
                imageHeight - 26,
                forceXp > 0 ? POSITIVE_STATUS_COLOR : NEGATIVE_STATUS_COLOR,
                true
        );
        guiGraphics.text(
                font,
                Component.translatable("gui.forcePowers.basePower", basePower),
                15,
                imageHeight - 15,
                basePower > 0 ? POSITIVE_STATUS_COLOR : NEGATIVE_STATUS_COLOR,
                true
        );
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mouseX = event.x();
        double mouseY = event.y();
        if (event.button() == 0 && isInsideTree(mouseX, mouseY)) {
            draggingTree = true;
            dragMoved = false;
            pressedMouseX = mouseX;
            pressedMouseY = mouseY;
            previousMouseX = mouseX;
            previousMouseY = mouseY;
            pressedPower = getPowerAt(mouseX, mouseY);
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseDragged(
            MouseButtonEvent event,
            double dragX,
            double dragY
    ) {
        double mouseX = event.x();
        double mouseY = event.y();
        if (event.button() == 0 && draggingTree) {
            double totalX = mouseX - pressedMouseX;
            double totalY = mouseY - pressedMouseY;
            if (!dragMoved
                    && totalX * totalX + totalY * totalY >= DRAG_THRESHOLD_SQUARED) {
                dragMoved = true;
            }
            if (dragMoved) {
                scrollX -= (mouseX - previousMouseX) * zoom;
                scrollY -= (mouseY - previousMouseY) * zoom;
                clampScroll();
            }
            previousMouseX = mouseX;
            previousMouseY = mouseY;
            return true;
        }
        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        double mouseX = event.x();
        double mouseY = event.y();
        if (event.button() == 0 && draggingTree) {
            Power releasedPower = getPowerAt(mouseX, mouseY);
            Power clickedPower = pressedPower;
            boolean shouldInvest = !dragMoved
                    && clickedPower != null
                    && clickedPower == releasedPower
                    && canInvest(clickedPower);
            draggingTree = false;
            dragMoved = false;
            pressedPower = null;
            if (shouldInvest) {
                investXp(clickedPower);
            }
            return true;
        }
        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseScrolled(
            double mouseX,
            double mouseY,
            double scrollXAmount,
            double scrollYAmount
    ) {
        if (!isInsideTree(mouseX, mouseY) || scrollYAmount == 0.0D) {
            return super.mouseScrolled(mouseX, mouseY, scrollXAmount, scrollYAmount);
        }

        float previousZoom = zoom;
        zoom = Mth.clamp(
                zoom - (float) Math.signum(scrollYAmount) * ZOOM_STEP,
                MIN_ZOOM,
                MAX_ZOOM
        );
        if (zoom != previousZoom) {
            double localMouseX = mouseX - leftPos - TREE_X;
            double localMouseY = mouseY - topPos - TREE_Y;
            scrollX += localMouseX * (previousZoom - zoom);
            scrollY += localMouseY * (previousZoom - zoom);
            clampScroll();
        }
        return true;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void renderForcestoneBackground(GuiGraphicsExtractor guiGraphics) {
        int scrollFloorX = Mth.floor(scrollX);
        int scrollFloorY = Mth.floor(scrollY);
        int tileOffsetX = Math.floorMod(
                scrollFloorX + BACKGROUND_COORDINATE_OFFSET,
                BACKGROUND_TILE_SIZE
        );
        int tileOffsetY = Math.floorMod(
                scrollFloorY + BACKGROUND_COORDINATE_OFFSET,
                BACKGROUND_TILE_SIZE
        );
        int firstTileX = Math.floorDiv(
                scrollFloorX + BACKGROUND_COORDINATE_OFFSET,
                BACKGROUND_TILE_SIZE
        );
        int firstTileY = Math.floorDiv(
                scrollFloorY + BACKGROUND_COORDINATE_OFFSET,
                BACKGROUND_TILE_SIZE
        );
        int contentWidth = Mth.ceil(TREE_WIDTH * zoom);
        int contentHeight = Mth.ceil(TREE_HEIGHT * zoom);

        int row = 0;
        for (int y = -tileOffsetY; y < contentHeight; y += BACKGROUND_TILE_SIZE) {
            int worldTileY = firstTileY + row;
            float brightness = Mth.clamp(
                    0.6F - worldTileY / 25.0F * 0.3F,
                    0.2F,
                    0.8F
            );
            int column = 0;
            for (int x = -tileOffsetX; x < contentWidth; x += BACKGROUND_TILE_SIZE) {
                int worldTileX = firstTileX + column;
                Identifier texture = getForcestoneTexture(worldTileX, worldTileY);
                guiGraphics.blit(
                        RenderPipelines.GUI_TEXTURED,
                        texture,
                        x,
                        y,
                        0.0F,
                        0.0F,
                        BACKGROUND_TILE_SIZE,
                        BACKGROUND_TILE_SIZE,
                        BACKGROUND_TILE_SIZE,
                        BACKGROUND_TILE_SIZE,
                        ARGB.gray(brightness)
                );
                column++;
            }
            row++;
        }
    }

    private void renderConnections(GuiGraphicsExtractor guiGraphics) {
        int scrollFloorX = Mth.floor(scrollX);
        int scrollFloorY = Mth.floor(scrollY);
        for (Power power : Power.POWERS) {
            if (power.parent == null || powerManager.getHierarchy(power) > 4) {
                continue;
            }

            int color = LOCKED_CONNECTION_COLOR;
            if (powerManager.hasPowerUnlocked(power)) {
                color = UNLOCKED_CONNECTION_COLOR;
            } else if (powerManager.canUnlockPower(power)) {
                color = AVAILABLE_CONNECTION_COLOR;
            }

            int nodeX = getPowerWorldX(power) - scrollFloorX + NODE_SIZE / 2;
            int nodeY = getPowerWorldY(power) - scrollFloorY + NODE_SIZE / 2;
            int parentX = getPowerWorldX(power.parent)
                    - scrollFloorX
                    + NODE_SIZE / 2;
            int parentY = getPowerWorldY(power.parent)
                    - scrollFloorY
                    + NODE_SIZE / 2;
            drawConnection(guiGraphics, nodeX, nodeY, parentX, parentY, color);
        }
    }

    private void renderPowerNodes(GuiGraphicsExtractor guiGraphics) {
        int scrollFloorX = Mth.floor(scrollX);
        int scrollFloorY = Mth.floor(scrollY);
        for (Power power : Power.POWERS) {
            int hierarchy = powerManager.getHierarchy(power);
            int nodeX = getPowerWorldX(power) - scrollFloorX;
            int nodeY = getPowerWorldY(power) - scrollFloorY;
            if (!isNodeVisible(nodeX, nodeY, hierarchy)) {
                continue;
            }

            boolean unlocked = powerManager.hasPowerUnlocked(power);
            boolean available = powerManager.canUnlockPower(power);
            float brightness = getNodeBrightness(unlocked, available, hierarchy);
            guiGraphics.blit(
                    RenderPipelines.GUI_TEXTURED,
                    GUI_TEXTURE,
                    nodeX - NODE_FRAME_OFFSET,
                    nodeY - NODE_FRAME_OFFSET,
                    0,
                    PANEL_HEIGHT,
                    NODE_FRAME_SIZE,
                    NODE_FRAME_SIZE,
                    256,
                    256,
                    ARGB.gray(brightness)
            );

            if (power.hasIcon()) {
                float iconBrightness = available ? brightness : 0.1F;
                guiGraphics.blit(
                        RenderPipelines.GUI_TEXTURED,
                        ICONS_TEXTURE,
                        nodeX + NODE_ICON_OFFSET,
                        nodeY + NODE_ICON_OFFSET,
                        power.getIconX() * 16,
                        power.getIconY() * 16,
                        16,
                        16,
                        256,
                        256,
                        ARGB.gray(iconBrightness)
                );
            }
        }
    }

    private void drawConnection(
            GuiGraphicsExtractor guiGraphics,
            int startX,
            int startY,
            int endX,
            int endY,
            int color
    ) {
        double deltaX = endX - startX;
        double deltaY = endY - startY;
        int length = Mth.ceil(Math.sqrt(deltaX * deltaX + deltaY * deltaY));
        if (length == 0) {
            return;
        }

        float angle = (float) Math.atan2(deltaY, deltaX);
        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().translate(startX, startY);
        guiGraphics.pose().rotate(angle);
        guiGraphics.fill(0, -1, length, 1, color);
        guiGraphics.pose().popMatrix();
    }

    private void renderPowerTooltip(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
        Power power = getPowerAt(mouseX, mouseY);
        if (power == null || minecraft == null || minecraft.player == null) {
            return;
        }

        int hierarchy = powerManager.getHierarchy(power);
        if (!powerManager.canUnlockPower(power)) {
            if (hierarchy > 3 || power.parent == null) {
                return;
            }
            Component tooltipTitle = hierarchy == 3
                    ? Component.translatable("gui.forcePowers.unknown")
                    : Component.literal(power.getLocalizedName());
            List<Component> lines = List.of(
                    tooltipTitle.copy().withStyle(ChatFormatting.DARK_GRAY),
                    Component.translatable(
                            "gui.forcePowers.requires",
                            power.parent.getLocalizedName()
                    ).withStyle(ChatFormatting.DARK_RED)
            );
            guiGraphics.setTooltipForNextFrame(
                    font,
                    lines,
                    Optional.empty(),
                    mouseX,
                    mouseY
            );
            return;
        }

        Player player = minecraft.player;
        boolean unlocked = powerManager.hasPowerUnlocked(power);
        PowerStats stats = power.powerStats;
        List<Component> lines = new ArrayList<>();
        lines.add(Component.literal(power.getLocalizedName()).withStyle(ChatFormatting.WHITE));

        int xpCost = power.getActualXpCost(player);
        if (xpCost != 0) {
            lines.add(Component.translatable("forcepower.cost", xpCost).withStyle(
                    unlocked ? ChatFormatting.GRAY : ChatFormatting.RED
            ));
        }
        if (stats.baseRequirement != 0) {
            boolean missingBasePower = !unlocked
                    && ALData.BASE_POWER.get(player) < stats.baseRequirement;
            lines.add(Component.translatable(
                    "forcepower.basePowerReq",
                    stats.baseRequirement
            ).withStyle(missingBasePower ? ChatFormatting.RED : ChatFormatting.GRAY));
        }
        if (stats.useCost != 0) {
            String translationKey = switch (stats.powerType) {
                case PER_USE -> "forcepower.perUse";
                case PER_SECOND -> "forcepower.perSecond";
                case PASSIVE -> "forcepower.passive";
            };
            lines.add(Component.translatable(
                    translationKey,
                    ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(stats.useCost)
            ).withStyle(ChatFormatting.GRAY));
        }
        if (stats.baseBonus != 0) {
            lines.add(Component.translatable(
                    "forcepower.basePower",
                    formatSigned(stats.baseBonus)
            ).withStyle(ChatFormatting.GRAY));
        }
        if (stats.forceBonus != 0) {
            lines.add(Component.translatable(
                    "forcepower.forcePower",
                    formatSigned(stats.forceBonus)
            ).withStyle(ChatFormatting.GRAY));
        }
        if (stats.regen != 0) {
            String regen = formatSigned(stats.regen)
                    + (stats.regenOperation == 1 ? "%" : "");
            lines.add(Component.translatable(
                    "forcepower.forceRegen",
                    regen
            ).withStyle(ChatFormatting.GRAY));
        }
        if (power.powerEffect != null) {
            String[] descriptions = power.powerEffect.getDesc();
            if (descriptions.length > 0) {
                lines.add(Component.empty());
                for (String description : descriptions) {
                    lines.add(Component.literal(description).withStyle(ChatFormatting.GRAY));
                }
            }
        }

        PowerData data = powerManager.getPowerData(power);
        int investedXp = data == null ? 0 : data.xpInvested;
        Component status = unlocked
                ? Component.translatable("forcepower.unlocked")
                : Component.translatable(
                        "forcepower.xpLeft",
                        Math.max(xpCost - investedXp, 0)
                );
        lines.add(status.copy().withStyle(style -> style.withColor(STATUS_COLOR)));
        guiGraphics.setTooltipForNextFrame(
                font,
                lines,
                Optional.empty(),
                mouseX,
                mouseY
        );
    }

    private Power getPowerAt(double mouseX, double mouseY) {
        if (!isInsideTree(mouseX, mouseY)) {
            return null;
        }

        double worldX = scrollX + (mouseX - leftPos - TREE_X) * zoom;
        double worldY = scrollY + (mouseY - topPos - TREE_Y) * zoom;
        for (Power power : Power.POWERS) {
            if (powerManager.getHierarchy(power) > 4) {
                continue;
            }
            int powerX = getPowerWorldX(power);
            int powerY = getPowerWorldY(power);
            if (worldX >= powerX && worldX <= powerX + NODE_SIZE
                    && worldY >= powerY && worldY <= powerY + NODE_SIZE) {
                return power;
            }
        }
        return null;
    }

    private boolean canInvest(Power power) {
        if (minecraft == null || minecraft.player == null
                || powerManager.hasPowerUnlocked(power)
                || !powerManager.canUnlockPower(power)) {
            return false;
        }

        Player player = minecraft.player;
        return (Mth.floor(ALData.FORCE_XP.get(player)) > 0
                || power.getActualXpCost(player) == 0)
                && (ALData.BASE_POWER.get(player) >= power.powerStats.baseRequirement
                || power.powerStats.baseRequirement == 0);
    }

    private void investXp(Power power) {
        Player player = minecraft.player;
        PowerManager.InvestResult result = PowerManager.investXp(player, power);
        if (result == PowerManager.InvestResult.NONE) {
            return;
        }

        ALNetworkManager.sendToServer(new MessageUnlockPower(power));
        if (result == PowerManager.InvestResult.UNLOCKED) {
            Lightsabers.proxy.playLocalSound(
                    player,
                    ALSounds.block_holocron_unlock,
                    1.0F,
                    1.0F
            );
        } else {
            RandomSource random = player.getRandom();
            Lightsabers.proxy.playLocalSound(
                    player,
                    ALSounds.block_holocron_invest,
                    1.0F,
                    1.1F + (random.nextFloat() - random.nextFloat()) * 0.2F
            );
        }
    }

    private Identifier getForcestoneTexture(int tileX, int tileY) {
        int depthRollBound = Math.max(1 + (tileX + 10) / 6, 1);
        int depthRoll = Math.floorMod(mixTileCoordinates(tileX, tileY, 0), depthRollBound);
        int sideIndex = depthRoll + tileX - 1 < 20 ? 0 : 1;
        int variantRoll = Math.floorMod(mixTileCoordinates(tileX, tileY, 1), 50);
        int variantIndex = variantRoll > 40 ? 2 : variantRoll > 32 ? 1 : 0;
        return FORCESTONE_TEXTURES[sideIndex][variantIndex];
    }

    private int mixTileCoordinates(int tileX, int tileY, int salt) {
        int hash = backgroundSeed;
        hash = 31 * hash + tileX;
        hash = 31 * hash + tileY;
        hash = 31 * hash + salt;
        hash ^= hash >>> 16;
        hash *= 0x7FEB352D;
        hash ^= hash >>> 15;
        return hash;
    }

    private void clampScroll() {
        scrollX = clampScrollAxis(
                scrollX,
                minPowerX - CONTENT_PADDING,
                maxPowerX + CONTENT_PADDING,
                TREE_WIDTH * zoom
        );
        scrollY = clampScrollAxis(
                scrollY,
                minPowerY - CONTENT_PADDING,
                maxPowerY + CONTENT_PADDING,
                TREE_HEIGHT * zoom
        );
    }

    private static double clampScrollAxis(
            double scroll,
            int contentStart,
            int contentEnd,
            double viewportSize
    ) {
        double maximumScroll = contentEnd - viewportSize;
        if (maximumScroll < contentStart) {
            return (contentStart + maximumScroll) / 2.0D;
        }
        return Mth.clamp(scroll, contentStart, maximumScroll);
    }

    private boolean isInsideTree(double mouseX, double mouseY) {
        int treeLeft = leftPos + TREE_X;
        int treeTop = topPos + TREE_Y;
        return mouseX >= treeLeft
                && mouseX < treeLeft + TREE_WIDTH
                && mouseY >= treeTop
                && mouseY < treeTop + TREE_HEIGHT;
    }

    private boolean isNodeVisible(int nodeX, int nodeY, int hierarchy) {
        return hierarchy <= 4
                && nodeX >= -NODE_SPACING
                && nodeY >= -NODE_SPACING
                && nodeX <= TREE_WIDTH * zoom
                && nodeY <= TREE_HEIGHT * zoom;
    }

    private static float getNodeBrightness(
            boolean unlocked,
            boolean available,
            int hierarchy
    ) {
        if (unlocked) {
            return 0.75F;
        }
        if (available) {
            return 1.0F;
        }
        if (hierarchy < 3) {
            return 0.3F;
        }
        return hierarchy == 3 ? 0.2F : 0.1F;
    }

    private static int getPowerWorldX(Power power) {
        return power.getXOffset() * NODE_SPACING;
    }

    private static int getPowerWorldY(Power power) {
        return power.getYOffset() * NODE_SPACING;
    }

    private static String formatSigned(int value) {
        return value > 0 ? "+" + value : Integer.toString(value);
    }

    private static Identifier blockTexture(String name) {
        return Identifier.fromNamespaceAndPath(
                Lightsabers.MODID,
                "textures/block/" + name + ".png"
        );
    }
}
