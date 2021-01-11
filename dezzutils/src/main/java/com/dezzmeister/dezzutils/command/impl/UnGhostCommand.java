package com.dezzmeister.dezzutils.command.impl;

import java.lang.reflect.Method;

import com.dezzmeister.dezzutils.invis.HiddenPlayers;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

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
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

public class UnGhostCommand {
	
	private static Method onFinishedPotionEffect = null;
	
	static {
		try {
			onFinishedPotionEffect.setAccessible(true);
			onFinishedPotionEffect = ObfuscationReflectionHelper.findMethod(ServerPlayerEntity.class, "func_70688_c", EffectInstance.class); // Obfuscated 'onFinishedPotionEffect'
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static final void register(CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(Commands.literal("unghost").requires(c -> c.hasPermissionLevel(0)).executes(c -> unghost(c)));
	}
	
	private static final int unghost(final CommandContext<CommandSource> context) throws CommandSyntaxException {		
		final CommandSource source = context.getSource();
		final ServerPlayerEntity player = source.asPlayer();
		
		if (onFinishedPotionEffect != null && player.isPotionActive(Effects.INVISIBILITY)) {
			final EffectInstance effectInstance = player.removeActivePotionEffect(Effects.INVISIBILITY);
			try {
				onFinishedPotionEffect.invoke(player, effectInstance);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		if (!HiddenPlayers.PLAYERLIST.contains(player)) {
			final IFormattableTextComponent errorMsg = new StringTextComponent("You are not a ghost!");
			errorMsg.func_240701_a_(TextFormatting.RED);
			
			throw (new SimpleCommandExceptionType(errorMsg)).create();
		}
		
		final MinecraftServer server = source.getServer();
		final PlayerList list = server.getPlayerList();
		
		list.sendPacketToAllPlayers(new SPlayerListItemPacket(SPlayerListItemPacket.Action.ADD_PLAYER, player));
		
		// Send a 'player has joined' message		
		list.func_232641_a_((ITextComponent)(new TranslationTextComponent("multiplayer.player.joined", new Object[] { player.getDisplayName() })).func_240699_a_(TextFormatting.YELLOW), ChatType.SYSTEM, Util.field_240973_b_);
		
		HiddenPlayers.PLAYERLIST.remove(player);
		
		return 1;
	}
}
