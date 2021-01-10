package com.dezzmeister.dezzutils.command.impl;

import com.mojang.brigadier.CommandDispatcher;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.MessageArgument;
import net.minecraft.entity.Entity;
import net.minecraft.util.Util;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

public class GSayCommand {
	public static void register(CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(Commands.literal("gsay").requires((p_198627_0_) -> {
			return p_198627_0_.hasPermissionLevel(2);
		}).then(Commands.argument("message", MessageArgument.message()).executes((p_198626_0_) -> {
			ITextComponent itextcomponent = MessageArgument.getMessage(p_198626_0_, "message");
			final IFormattableTextComponent translationtextcomponent = getFullMessage(p_198626_0_.getSource().getDisplayName(), itextcomponent);
			Entity entity = p_198626_0_.getSource().getEntity();
			if (entity != null) {
				p_198626_0_.getSource().getServer().getPlayerList().func_232641_a_(translationtextcomponent,
						ChatType.CHAT, entity.getUniqueID());
			} else {
				p_198626_0_.getSource().getServer().getPlayerList().func_232641_a_(translationtextcomponent,
						ChatType.SYSTEM, Util.field_240973_b_);
			}

			return 1;
		})));
	}
	
	private static IFormattableTextComponent getFullMessage(final ITextComponent name, final ITextComponent message) {
		final IFormattableTextComponent leftAngle = new StringTextComponent("<");
		final IFormattableTextComponent rightAngle = new StringTextComponent("> ");
		
		return leftAngle.func_230529_a_(name).func_230529_a_(rightAngle).func_230529_a_(message);
	}
}
