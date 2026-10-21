package com.ovo.lastsongofelysian.event;

import com.ovo.lastsongofelysian.util.ModText;

import com.ovo.lastsongofelysian.lastsongofelysian;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = lastsongofelysian.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class CocoonTrialCommands {

    private CocoonTrialCommands() {
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("lastsongofelysian")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.literal("complete_all_trials")
                                .executes(context -> complete(
                                        context.getSource(),
                                        context.getSource().getPlayerOrException()
                                ))
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(context -> complete(
                                                context.getSource(),
                                                EntityArgument.getPlayer(context, "player")
                                        )))
                        )
        );
    }

    private static int complete(CommandSourceStack source, ServerPlayer player) {
        CocoonTrialProgress.completeAll(player);
        source.sendSuccess(
                () -> ModText.literal("已完成 " + player.getGameProfile().getName() + " 的全部终焉试炼"),
                true
        );
        return 1;
    }
}
