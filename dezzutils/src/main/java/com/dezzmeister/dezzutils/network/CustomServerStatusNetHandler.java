package com.dezzmeister.dezzutils.network;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import com.dezzmeister.dezzutils.invis.HiddenPlayers;
import com.mojang.authlib.GameProfile;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.ServerStatusResponse;
import net.minecraft.network.ServerStatusResponse.Players;
import net.minecraft.network.status.ServerStatusNetHandler;
import net.minecraft.network.status.client.CPingPacket;
import net.minecraft.network.status.client.CServerQueryPacket;
import net.minecraft.network.status.server.SPongPacket;
import net.minecraft.network.status.server.SServerInfoPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

public class CustomServerStatusNetHandler extends ServerStatusNetHandler {
	private static final ITextComponent EXIT_MESSAGE = new TranslationTextComponent(
			"multiplayer.status.request_handled");
	private final MinecraftServer server;
	private final NetworkManager networkManager;
	private boolean handled;

	public CustomServerStatusNetHandler(MinecraftServer serverIn, NetworkManager netManager) {
		super(serverIn, netManager);
		this.server = serverIn;
		this.networkManager = netManager;
	}

	/**
	 * Invoked when disconnecting, the parameter is a ChatComponent describing the
	 * reason for termination
	 */
	public void onDisconnect(ITextComponent reason) {
	}

	/**
	 * Returns this the NetworkManager instance registered with this
	 * NetworkHandlerPlayClient
	 */
	public NetworkManager getNetworkManager() {
		return this.networkManager;
	}

	public void processServerQuery(CServerQueryPacket packetIn) {		
		if (this.handled) {
			this.networkManager.closeChannel(EXIT_MESSAGE);
		} else {
			this.handled = true;
			final ServerStatusResponse trueStatus = this.server.getServerStatusResponse();
			final ServerStatusResponse response = copyServerStatus(trueStatus);
			removeGhosts(response);
			this.networkManager.sendPacket(new SServerInfoPacket(response));
		}
	}
	
	private static Field onlinePlayerCountField;
	
	public static void init() throws NoSuchFieldException, SecurityException {
		onlinePlayerCountField = ObfuscationReflectionHelper.findField(Players.class, "field_151334_b"); // Obfuscated 'onlinePlayerCount'
		onlinePlayerCountField.setAccessible(true);
	}
	
	private ServerStatusResponse copyServerStatus(final ServerStatusResponse status) {
		final ServerStatusResponse out = new ServerStatusResponse();
		out.setServerDescription(status.getServerDescription());
		out.setPlayers(copyPlayers(status.getPlayers()));
		out.setVersion(status.getVersion());
		out.setFavicon(status.getFavicon());
		out.setForgeData(status.getForgeData());
		
		return out;
	}
	
	private Players copyPlayers(final Players players) {
		final Players out = new Players(players.getMaxPlayers(), players.getOnlinePlayerCount());
		out.setPlayers(players.getPlayers());
		
		return out;
	}
	
	private void removeGhosts(final ServerStatusResponse status) {
		final Players players = status.getPlayers();
		final GameProfile[] profiles = players.getPlayers();
		final List<GameProfile> finalProfiles = new ArrayList<GameProfile>();
		
		for (final GameProfile profile : profiles) {
			finalProfiles.add(profile);
		}
		
		for (int i = finalProfiles.size() - 1; i >= 0; i --) {
			final GameProfile profile = finalProfiles.get(i);
			
			for (final ServerPlayerEntity ghost : HiddenPlayers.PLAYERLIST) {
				if (profile.getName().equals(ghost.getName().getString())) {
					finalProfiles.remove(i);
					break;
				}
			}
		}
		
		final GameProfile[] profilesOut = finalProfiles.toArray(new GameProfile[finalProfiles.size()]);
		players.setPlayers(profilesOut);
		try {
			onlinePlayerCountField.set(players, profilesOut.length);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
		
		status.setPlayers(players);
	}

	public void processPing(CPingPacket packetIn) {
		this.networkManager.sendPacket(new SPongPacket(packetIn.getClientTime()));
		this.networkManager.closeChannel(EXIT_MESSAGE);
	}
}
