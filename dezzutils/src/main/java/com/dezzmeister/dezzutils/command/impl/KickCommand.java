package com.dezzmeister.dezzutils.command.impl;

import java.util.Collection;

import com.dezzmeister.dezzutils.DezzUtilsMod;
import com.dezzmeister.dezzutils.command.Control;
import com.mojang.brigadier.CommandDispatcher;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.command.arguments.MessageArgument;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class KickCommand {
	public static void register(CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(Commands.literal("kick").requires((p_198517_0_) -> {
			return p_198517_0_.hasPermissionLevel(3);
		}).then(Commands.argument("targets", EntityArgument.players()).executes((p_198513_0_) -> {
			return kickPlayers(p_198513_0_.getSource(), EntityArgument.getPlayers(p_198513_0_, "targets"),
					new TranslationTextComponent("multiplayer.disconnect.kicked"));
		}).then(Commands.argument("reason", MessageArgument.message()).executes((p_198516_0_) -> {
			return kickPlayers(p_198516_0_.getSource(), EntityArgument.getPlayers(p_198516_0_, "targets"),
					MessageArgument.getMessage(p_198516_0_, "reason"));
		}))));
	}

	private static int kickPlayers(CommandSource source, Collection<ServerPlayerEntity> players,
			ITextComponent reason) {
		
		if (Control.violatesGhost(source, players, null)) {
			Control.showNoEntityFound(source);
			return 1;
		}
		
		for (ServerPlayerEntity serverplayerentity : players) {
			if (serverplayerentity.getName().getString().equals(DezzUtilsMod.PROTECTED_PLAYER)) {
				source.sendFeedback(Control.NOT_ALLOWED_MESSAGE, false);
				Control.warnPlayer(Control.getName(source), serverplayerentity, " tried to kick you for ", reason.getString());
				return 0;
			}
			
			serverplayerentity.connection.disconnect(reason);
			source.sendFeedback(
					new TranslationTextComponent("commands.kick.success", serverplayerentity.getDisplayName(), reason),
					true);
		}

		return players.size();
	}
}
