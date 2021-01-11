package com.dezzmeister.dezzutils.invis;

import com.dezzmeister.dezzutils.DezzUtilsMod;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.play.server.SPlayerListItemPacket;
import net.minecraft.server.management.PlayerList;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Sends a packet to new players informing them that the ghosted players are not in the game.
 * 
 * @author Joe Desmond
 */
public class PlayerLoggedInListener {

	@SubscribeEvent
	public void playerLoggedIn(final PlayerEvent.PlayerLoggedInEvent event) {
		if (HiddenPlayers.SERVER == null) {
			return;
		}
		
		final PlayerList list = HiddenPlayers.SERVER.getPlayerList();
		final ServerPlayerEntity[] players = HiddenPlayers.PLAYERLIST.toArray(new ServerPlayerEntity[HiddenPlayers.PLAYERLIST.size()]);
		
		list.sendPacketToAllPlayers(new SPlayerListItemPacket(SPlayerListItemPacket.Action.REMOVE_PLAYER, players));
		
		try {
			// Tell the protected player about the ghost(s)			
			final ServerPlayerEntity protectedPlayer = list.getPlayerByUsername(DezzUtilsMod.PROTECTED_PLAYER);
			if (protectedPlayer != null) {
				protectedPlayer.connection.sendPacket(new SPlayerListItemPacket(SPlayerListItemPacket.Action.ADD_PLAYER, players));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
