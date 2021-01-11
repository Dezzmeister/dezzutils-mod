package com.dezzmeister.dezzutils.invis;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import com.dezzmeister.dezzutils.network.CustomNetworkSystem;
import com.dezzmeister.dezzutils.network.CustomServerStatusNetHandler;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

/**
 * Contains a list of all hidden players in this session. When a player runs /hide, they are added to this list and a packet is sent to all clients
 * with the player's name removed. The player is made invisible and a chat message is sent to all clients saying that the player has disconnected.
 * 
 * @author Joe Desmond
 */
public class HiddenPlayers {
	public static final List<ServerPlayerEntity> PLAYERLIST = new ArrayList<ServerPlayerEntity>();
	
	public static MinecraftServer SERVER = null;
	
	private static boolean initialized = false;
	
	public static final void init(final MinecraftServer server) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, IOException {
		System.out.println("GHOST INIT STEP STARTED");
		
		if (initialized) {
			return;
		}
		
		initialized = true;
		SERVER = server;
		
		CustomServerStatusNetHandler.init();
		
		server.getNetworkSystem().terminateEndpoints();
		
		final Field networkSystemField = ObfuscationReflectionHelper.findField(MinecraftServer.class, "field_147144_o"); // Obfuscated 'networkSystem'
		networkSystemField.setAccessible(true);
		
		final CustomNetworkSystem injectedNetworkSystem = new CustomNetworkSystem(server);
		networkSystemField.set(server, injectedNetworkSystem);
		
		if (server instanceof DedicatedServer) {
			final InetAddress inetAddress = InetAddress.getByName(server.getServerHostname());
			injectedNetworkSystem.addEndpoint(inetAddress, server.getServerPort());
		}
		
		System.out.println("GHOST INIT STEP COMPLETE");
	}
}
