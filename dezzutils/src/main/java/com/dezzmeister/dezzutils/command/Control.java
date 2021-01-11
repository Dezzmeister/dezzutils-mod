package com.dezzmeister.dezzutils.command;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.dezzmeister.dezzutils.DezzUtilsMod;
import com.dezzmeister.dezzutils.invis.HiddenPlayers;

import net.minecraft.command.CommandSource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

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
		player.sendMessage(msg, player.getUniqueID());
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
	
	public static final void showNoEntityFound(final CommandSource target) {
		if (target == null) {
			return;
		}
		
		final IFormattableTextComponent component = new TranslationTextComponent("argument.entity.notfound.entity");
		component.func_240699_a_(TextFormatting.RED);
		
		target.sendFeedback(component, false);
	}
	
	public static final boolean violatesGhost(final CommandSource source, Collection<? extends Entity> targets, final Entity destination) {
		final ServerPlayerEntity caller;
		
		try {
			caller = source.asPlayer();
		} catch (Exception e) {
			return false;
		}
		
		if (caller.getName().getString().equals(DezzUtilsMod.PROTECTED_PLAYER)) {
			return false;
		}
		
		if (destination != null && HiddenPlayers.PLAYERLIST.contains(destination) && !caller.equals(destination)) {
			return true;
		}
		
		for (final ServerPlayerEntity ghost : HiddenPlayers.PLAYERLIST) {
			if (targets.contains(ghost) && !caller.equals(ghost)) {
				targets.remove(ghost);
			}
		}
		
		return (targets.size() == 0);
	}
	
	
	@SafeVarargs
	public static final <T> List<T> listOf(T... ts) {
		final List<T> list = new ArrayList<T>();
		for (final T t : ts) {
			list.add(t);
		}
		
		return list;
	}
}
