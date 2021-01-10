package com.dezzmeister.dezzutils.command;

import net.minecraft.command.CommandSource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

public class Control {
	public static final StringTextComponent NOT_ALLOWED_MESSAGE;

	static {
		NOT_ALLOWED_MESSAGE = new StringTextComponent("YOU DO NOT HAVE PERMISSION TO DO THAT");
		NOT_ALLOWED_MESSAGE.func_240701_a_(TextFormatting.DARK_RED, TextFormatting.BOLD);
	}
	
	public static final IFormattableTextComponent getProtectedWarningText(final String caller, final String message) {
		final StringTextComponent playerComponent = new StringTextComponent(caller);
		playerComponent.func_240701_a_(TextFormatting.YELLOW);
		
		final StringTextComponent warningComponent = new StringTextComponent(message);
		warningComponent.func_240701_a_(TextFormatting.RED);
		
		return playerComponent.func_230529_a_(warningComponent);
	}
	
	public static final void warnPlayer(final String caller, final Entity player, final String message, final String destString) {
		final StringTextComponent posComponent = new StringTextComponent(destString);
		posComponent.func_240701_a_(TextFormatting.BLUE);
		
		final IFormattableTextComponent msg = getProtectedWarningText(caller, message).func_230529_a_(posComponent);
		player.sendMessage(msg, null);
	}

	public static final String getName(final CommandSource source) {
		try {
			final ServerPlayerEntity player = source.asPlayer();
			return player.getName().getString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return source.getName();
	}
}
