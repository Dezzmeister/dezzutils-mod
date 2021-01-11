package com.dezzmeister.dezzutils.command.impl;

import com.dezzmeister.dezzutils.DezzUtilsMod;
import com.dezzmeister.dezzutils.invis.HiddenPlayers;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.play.server.SPlayerListItemPacket;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.Util;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.Color;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

public class GhostCommand {
	static final Color TEXT_WHITE = Color.func_240744_a_(TextFormatting.WHITE);
	
	public static final void register(CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(Commands.literal("ghost").requires(c -> c.hasPermissionLevel(0)).executes(c -> ghost(c)));
	}
	
	private static final int ghost(final CommandContext<CommandSource> context) throws CommandSyntaxException {
		final CommandSource source = context.getSource();
		final ServerPlayerEntity player = source.asPlayer();
		
		final MinecraftServer server = source.getServer();
		final PlayerList list = server.getPlayerList();
		
		list.sendPacketToAllPlayers(new SPlayerListItemPacket(SPlayerListItemPacket.Action.REMOVE_PLAYER, player));
		
		// Send a 'player has left' message
		list.func_232641_a_((ITextComponent)(new TranslationTextComponent("multiplayer.player.left", new Object[] { player.getDisplayName() })).func_240699_a_(TextFormatting.YELLOW), ChatType.SYSTEM, Util.field_240973_b_);
		
		try {
			// Tell the protected player that a player is a ghost			
			final ServerPlayerEntity protectedPlayer = list.getPlayerByUsername(DezzUtilsMod.PROTECTED_PLAYER);
			
			if (protectedPlayer != null) {
				protectedPlayer.sendMessage(getGhostNotification(player), Util.field_240973_b_);
				protectedPlayer.connection.sendPacket(new SPlayerListItemPacket(SPlayerListItemPacket.Action.ADD_PLAYER, player));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
				
		final EffectInstance effectInstance = new EffectInstance(Effects.INVISIBILITY, 86400, 1, false, false);
		player.addPotionEffect(effectInstance);
		
		HiddenPlayers.PLAYERLIST.add(player);
		
		return 1;
	}
	
	private static final IFormattableTextComponent getGhostNotification(final ServerPlayerEntity ghost) {
		final IFormattableTextComponent emptyComponent = new StringTextComponent("");
		final ITextComponent displayName = ghost.getDisplayName();
		final IFormattableTextComponent message = new StringTextComponent(" is a ghost");
		message.func_240699_a_(TextFormatting.YELLOW);
		
		return emptyComponent.func_230529_a_(displayName).func_230529_a_(message);
	}
}
