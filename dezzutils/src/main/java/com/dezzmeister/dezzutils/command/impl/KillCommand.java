package com.dezzmeister.dezzutils.command.impl;

import java.util.Collection;

import com.dezzmeister.dezzutils.DezzUtilsMod;
import com.dezzmeister.dezzutils.command.Control;
import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.CommandDispatcher;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.Entity;
import net.minecraft.util.text.TranslationTextComponent;

public class KillCommand {
	public static void register(CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(Commands.literal("kill").requires((p_198521_0_) -> {
			return p_198521_0_.hasPermissionLevel(2);
		}).executes((p_198520_0_) -> {
			return killEntities(p_198520_0_.getSource(), ImmutableList.of(p_198520_0_.getSource().assertIsEntity()));
		}).then(Commands.argument("targets", EntityArgument.entities()).executes((p_229810_0_) -> {
			return killEntities(p_229810_0_.getSource(), EntityArgument.getEntities(p_229810_0_, "targets"));
		})));
	}

	private static int killEntities(CommandSource source, Collection<? extends Entity> targets) {
		for (Entity entity : targets) {
			System.out.println("ENTITY: " + entity.getName().getString());
			if (entity.getName().getString().equals(DezzUtilsMod.PROTECTED_PLAYER)) {
				source.sendFeedback(Control.NOT_ALLOWED_MESSAGE, false);
				Control.warnPlayer(Control.getName(source), entity, " tried to kill you", "");
				return 0;
			}
		}
		
		for (Entity entity : targets) {
			entity.onKillCommand();
		}

		if (targets.size() == 1) {
			source.sendFeedback(new TranslationTextComponent("commands.kill.success.single",
					targets.iterator().next().getDisplayName()), true);
		} else {
			source.sendFeedback(new TranslationTextComponent("commands.kill.success.multiple", targets.size()), true);
		}

		return targets.size();
	}
}
