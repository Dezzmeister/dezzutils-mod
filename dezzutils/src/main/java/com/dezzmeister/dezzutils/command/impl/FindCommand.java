package com.dezzmeister.dezzutils.command.impl;

import com.dezzmeister.dezzutils.DezzUtilsMod;
import com.dezzmeister.dezzutils.command.Control;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.Util;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

public class FindCommand {
	public static void register(CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(Commands.literal("find").requires((p_198627_0_) -> {
			return p_198627_0_.hasPermissionLevel(0);
		}).then(Commands.argument("player", EntityArgument.player()).executes((context) -> {
			return findPlayer(context);
		})));
	}
	
	private static int findPlayer(final CommandContext<CommandSource> context) throws CommandSyntaxException {
		final CommandSource source = context.getSource();
		final ServerPlayerEntity caller = source.asPlayer();
		final ServerPlayerEntity target = EntityArgument.getPlayer(context, "player");
		
		if (Control.violatesGhost(source, Control.listOf(target), null)) {
			Control.showNoEntityFound(source);
			return 1;
		}
		
		if (target.getName().getString().equals(DezzUtilsMod.PROTECTED_PLAYER)) {
			Control.warnPlayer(caller.getName().getString(), target, " found you", "");
		}
		
		final int x = (int) target.getPosX();
		final int y = (int) target.getPosY();
		final int z = (int) target.getPosZ();
		final String coordString = "(" + x + ", " + y + ", " + z + ")";
		
		final IFormattableTextComponent empty = new StringTextComponent("");
		final ITextComponent targetName = target.getDisplayName();
		final IFormattableTextComponent message = new StringTextComponent(" is located at ");
		message.func_240699_a_(TextFormatting.GREEN);
		final IFormattableTextComponent coords = new StringTextComponent(coordString);
		coords.func_240699_a_(TextFormatting.BLUE);
		
		final ITextComponent fullMessage = empty.func_230529_a_(targetName).func_230529_a_(message).func_230529_a_(coords);
		caller.sendMessage(fullMessage, Util.field_240973_b_);
		
		return 1;
	}
}
