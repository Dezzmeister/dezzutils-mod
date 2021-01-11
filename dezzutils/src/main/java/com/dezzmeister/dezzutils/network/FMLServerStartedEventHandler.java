package com.dezzmeister.dezzutils.network;

import com.dezzmeister.dezzutils.invis.HiddenPlayers;

import net.minecraft.server.MinecraftServer;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;

public class FMLServerStartedEventHandler {
	
	@SubscribeEvent
	public void handleServerStarted(FMLServerStartedEvent event) {
		System.out.println("HANDLING SERVER STARTED EVENT");
		final MinecraftServer server = event.getServer();
		try {
			HiddenPlayers.init(server);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
