package com.piti.ptm;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.piti.ptm.capability.PlayerRadiationData;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class PtmCommands {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("ptm")
                .requires(source -> source.hasPermission(2)) // Requires op
                .then(Commands.literal("radiation")

                        // ptm radiation get player [double]
                        .then(Commands.literal("get")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(context -> getRadiation(context.getSource(), EntityArgument.getPlayer(context, "player")))))

                        // ptm radiation set player [double]
                        .then(Commands.literal("set")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .then(Commands.argument("amount", DoubleArgumentType.doubleArg(0.0)) // Caps minimum at 0.0
                                                .executes(context -> setRadiation(context.getSource(),
                                                        EntityArgument.getPlayer(context, "player"),
                                                        DoubleArgumentType.getDouble(context, "amount"))))))
                )
        );
    }

    private static int getRadiation(CommandSourceStack source, ServerPlayer target) {
        target.getCapability(PlayerRadiationData.INSTANCE).ifPresent(data -> {
            source.sendSuccess(() -> Component.literal(target.getScoreboardName() + " currently has " + data.getRadExposure() + " Rads."), false);
        });
        return 1;
    }

    private static int setRadiation(CommandSourceStack source, ServerPlayer target, double amount) {
        target.getCapability(PlayerRadiationData.INSTANCE).ifPresent(data -> {
            data.setRadExposure(amount);
            source.sendSuccess(() -> Component.literal("Successfully set radiation for " + target.getScoreboardName() + " to " + amount), true);
        });
        return 1;
    }
}