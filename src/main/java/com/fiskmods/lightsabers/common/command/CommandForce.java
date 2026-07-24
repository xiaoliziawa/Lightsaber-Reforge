package com.fiskmods.lightsabers.common.command;

import com.fiskmods.lightsabers.common.data.ALData;
import com.fiskmods.lightsabers.common.force.Power;
import com.fiskmods.lightsabers.common.force.PowerData;
import com.fiskmods.lightsabers.common.force.PowerManager;
import com.fiskmods.lightsabers.common.generator.structure.EnumStructure;
import com.fiskmods.lightsabers.common.generator.worldgen.ModWorldgen;
import com.fiskmods.lightsabers.helper.ALHelper;
import com.mojang.datafixers.util.Pair;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.commands.PlaceCommand;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.bus.api.SubscribeEvent;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public final class CommandForce {
    private static final String TARGET = "target";
    private static final String AMOUNT = "amount";
    private static final String POWER = "power";
    private static final String STRUCTURE = "structure";
    private static final String RANGE = "range";
    private static final String X = "x";
    private static final String Z = "z";

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        register(event.getDispatcher());
    }

    private static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("force")
                .requires(source -> source.hasPermission(2));
        root.then(createNumericBranch("xp"));
        root.then(createNumericBranch("base")
                .then(Commands.literal("reset")
                        .executes(context -> resetBase(context, context.getSource().getPlayerOrException()))
                        .then(Commands.argument(TARGET, EntityArgument.player())
                                .executes(context -> resetBase(context, EntityArgument.getPlayer(context, TARGET))))));
        root.then(createPowerBranch());
        root.then(createStructureBranch());
        dispatcher.register(root);
    }

    private static LiteralArgumentBuilder<CommandSourceStack> createNumericBranch(String type) {
        LiteralArgumentBuilder<CommandSourceStack> branch = Commands.literal(type);
        for (NumericOperation operation : NumericOperation.values()) {
            branch.then(Commands.literal(operation.name().toLowerCase(Locale.ROOT))
                    .then(Commands.argument(AMOUNT, FloatArgumentType.floatArg(0.0F))
                            .executes(context -> changeValue(
                                    context,
                                    context.getSource().getPlayerOrException(),
                                    type,
                                    operation
                            ))
                            .then(Commands.argument(TARGET, EntityArgument.player())
                                    .executes(context -> changeValue(
                                            context,
                                            EntityArgument.getPlayer(context, TARGET),
                                            type,
                                            operation
                                    )))));
        }
        return branch;
    }

    private static LiteralArgumentBuilder<CommandSourceStack> createPowerBranch() {
        LiteralArgumentBuilder<CommandSourceStack> branch = Commands.literal("power");
        for (boolean give : new boolean[]{true, false}) {
            branch.then(Commands.literal(give ? "give" : "take")
                    .then(Commands.argument(POWER, StringArgumentType.word())
                            .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                                    Power.POWERS.stream().map(Power::getName),
                                    builder
                            ))
                            .executes(context -> changePower(
                                    context,
                                    context.getSource().getPlayerOrException(),
                                    give
                            ))
                            .then(Commands.argument(TARGET, EntityArgument.player())
                                    .executes(context -> changePower(
                                            context,
                                            EntityArgument.getPlayer(context, TARGET),
                                            give
                                    )))));
        }
        return branch;
    }

    private static LiteralArgumentBuilder<CommandSourceStack> createStructureBranch() {
        RequiredArgumentBuilder<CommandSourceStack, Integer> locateZ =
                Commands.argument(Z, IntegerArgumentType.integer())
                        .executes(context -> locateStructure(
                                context,
                                IntegerArgumentType.getInteger(context, X),
                                IntegerArgumentType.getInteger(context, Z)
                        ));
        RequiredArgumentBuilder<CommandSourceStack, Integer> locateX =
                Commands.argument(X, IntegerArgumentType.integer()).then(locateZ);
        RequiredArgumentBuilder<CommandSourceStack, Integer> range =
                Commands.argument(RANGE, IntegerArgumentType.integer(0, 1024))
                        .executes(context -> locateStructure(
                                context,
                                sourceX(context),
                                sourceZ(context)
                        ))
                        .then(locateX);

        RequiredArgumentBuilder<CommandSourceStack, Integer> generateZ =
                Commands.argument(Z, IntegerArgumentType.integer())
                        .executes(context -> generateStructure(
                                context,
                                IntegerArgumentType.getInteger(context, X),
                                IntegerArgumentType.getInteger(context, Z)
                        ));
        RequiredArgumentBuilder<CommandSourceStack, Integer> generateX =
                Commands.argument(X, IntegerArgumentType.integer()).then(generateZ);
        RequiredArgumentBuilder<CommandSourceStack, String> generateStructure =
                structureArgument(false)
                        .executes(context -> generateStructure(
                                context,
                                sourceX(context),
                                sourceZ(context)
                        ))
                        .then(generateX);

        return Commands.literal("structure")
                .requires(source -> source.hasPermission(3))
                .then(Commands.literal("locate").then(structureArgument(true).then(range)))
                .then(Commands.literal("generate").then(generateStructure));
    }

    private static RequiredArgumentBuilder<CommandSourceStack, String> structureArgument(
            boolean allowAll
    ) {
        return Commands.argument(STRUCTURE, StringArgumentType.word())
                .suggests((context, builder) -> {
                    if (allowAll) {
                        builder.suggest("*");
                    }
                    return SharedSuggestionProvider.suggest(
                            Arrays.stream(EnumStructure.values())
                                    .map(value -> ALHelper.getUnconventionalName(value.name())),
                            builder
                    );
                });
    }

    private static int locateStructure(
            CommandContext<CommandSourceStack> context,
            int x,
            int z
    ) {
        ServerLevel level = context.getSource().getLevel();
        if (level.dimension() != Level.OVERWORLD
                || level.isFlat()
                || !level.getServer().getWorldData().worldGenOptions().generateStructures()) {
            context.getSource().sendFailure(Component.translatable(
                    "commands.force.structure.locate.doesNotGenerate",
                    StringArgumentType.getString(context, STRUCTURE)
            ));
            return 0;
        }

        String name = StringArgumentType.getString(context, STRUCTURE);
        EnumStructure requested = name.equals("*") ? null : getStructure(name);
        if (!name.equals("*") && requested == null) {
            context.getSource().sendFailure(Component.translatable(
                    "commands.force.structure.unknown",
                    name
            ));
            return 0;
        }

        Registry<Structure> registry = level.registryAccess().registryOrThrow(
                Registries.STRUCTURE
        );
        List<Holder<Structure>> structures = Arrays.stream(EnumStructure.values())
                .filter(candidate -> requested == null || requested == candidate)
                .map(candidate -> registry.getHolderOrThrow(ModWorldgen.key(candidate)))
                .map(holder -> (Holder<Structure>) holder)
                .toList();
        Pair<BlockPos, Holder<Structure>> result = level.getChunkSource()
                .getGenerator()
                .findNearestMapStructure(
                        level,
                        HolderSet.direct(structures),
                        new BlockPos(x, level.getSeaLevel(), z),
                        IntegerArgumentType.getInteger(context, RANGE),
                        false
                );
        if (result == null) {
            context.getSource().sendFailure(Component.translatable(
                    "commands.force.structure.locate.failure"
            ));
            return 0;
        }
        EnumStructure found = result.getSecond()
                .unwrapKey()
                .map(key -> getStructure(key.location().getPath()))
                .orElse(requested);
        BlockPos position = result.getFirst();
        context.getSource().sendSuccess(
                () -> Component.translatable(
                        "commands.force.structure.locate.success",
                        ALHelper.getUnconventionalName(found.name()),
                        position.getX(),
                        position.getZ()
                ),
                false
        );
        return 1;
    }

    private static int generateStructure(
            CommandContext<CommandSourceStack> context,
            int x,
            int z
    ) throws CommandSyntaxException {
        String name = StringArgumentType.getString(context, STRUCTURE);
        EnumStructure structure = getStructure(name);
        if (structure == null) {
            context.getSource().sendFailure(Component.translatable(
                    "commands.force.structure.unknown",
                    name
            ));
            return 0;
        }
        Registry<Structure> registry = context.getSource()
                .registryAccess()
                .registryOrThrow(Registries.STRUCTURE);
        Holder.Reference<Structure> holder = registry.getHolderOrThrow(
                ModWorldgen.key(structure)
        );
        return PlaceCommand.placeStructure(
                context.getSource(),
                holder,
                new BlockPos(x, context.getSource().getLevel().getSeaLevel(), z)
        );
    }

    private static EnumStructure getStructure(String name) {
        for (EnumStructure structure : EnumStructure.values()) {
            if (ALHelper.getUnconventionalName(structure.name()).equalsIgnoreCase(name)) {
                return structure;
            }
        }
        return null;
    }

    private static int sourceX(CommandContext<CommandSourceStack> context) {
        return (int) Math.floor(context.getSource().getPosition().x);
    }

    private static int sourceZ(CommandContext<CommandSourceStack> context) {
        return (int) Math.floor(context.getSource().getPosition().z);
    }

    private static int changeValue(
            CommandContext<CommandSourceStack> context,
            ServerPlayer player,
            String type,
            NumericOperation operation
    ) {
        float amount = FloatArgumentType.getFloat(context, AMOUNT);
        float current = type.equals("xp") ? ALData.FORCE_XP.get(player) : ALData.BASE_POWER.get(player);
        float result = Math.max(0.0F, operation.apply(current, amount));
        if (type.equals("xp")) {
            ALData.FORCE_XP.set(player, result);
        } else {
            ALData.BASE_POWER.set(player, (byte) Math.min(Byte.MAX_VALUE, (int) result));
        }
        String key = "commands.force." + type + "." + operation.name().toLowerCase(Locale.ROOT) + ".success";
        Component message = operation == NumericOperation.SET
                ? Component.translatable(key, player.getDisplayName(), (int) amount)
                : Component.translatable(key, (int) amount, player.getDisplayName());
        context.getSource().sendSuccess(() -> message, true);
        return 1;
    }

    private static int resetBase(CommandContext<CommandSourceStack> context, ServerPlayer player) {
        ALData.BASE_POWER.set(player, ALHelper.getBasePower(player));
        context.getSource().sendSuccess(
                () -> Component.translatable("commands.force.base.reset.success", player.getDisplayName()),
                true
        );
        return 1;
    }

    private static int changePower(
            CommandContext<CommandSourceStack> context,
            ServerPlayer player,
            boolean give
    ) throws CommandSyntaxException {
        String name = StringArgumentType.getString(context, POWER);
        boolean all = name.equals("*");
        Power power = Power.getPowerFromName(name);
        if (!all && power == null) {
            throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownArgument().create();
        }

        if (all) {
            for (PowerData data : ALData.POWERS.get(player)) {
                if (give) {
                    PowerManager.unlockPower(player, data.power);
                } else {
                    PowerManager.removePower(player, data.power);
                }
            }
            context.getSource().sendSuccess(
                    () -> Component.translatable(
                            give ? "commands.force.power.give.success.all" : "commands.force.power.take.success.all",
                            player.getDisplayName()
                    ),
                    true
            );
        } else {
            boolean changed = give
                    ? PowerManager.unlockPower(player, power)
                    : PowerManager.removePower(player, power);
            if (!changed) {
                String key = give
                        ? "commands.force.power.alreadyHave"
                        : "commands.force.power.dontHave";
                context.getSource().sendFailure(Component.translatable(key, player.getDisplayName(), name));
                return 0;
            }
            context.getSource().sendSuccess(
                    () -> Component.translatable(
                            give ? "commands.force.power.give.success.one" : "commands.force.power.take.success.one",
                            give ? player.getDisplayName() : power.getChatFormattedName(),
                            give ? power.getChatFormattedName() : player.getDisplayName()
                    ),
                    true
            );
        }
        ALData.POWERS.sync(player);
        ALData.BASE_POWER.sync(player);
        return 1;
    }

    private enum NumericOperation {
        GIVE {
            @Override
            float apply(float current, float amount) {
                return current + amount;
            }
        },
        TAKE {
            @Override
            float apply(float current, float amount) {
                return current - amount;
            }
        },
        SET {
            @Override
            float apply(float current, float amount) {
                return amount;
            }
        };

        abstract float apply(float current, float amount);
    }
}
