package com.dezzmeister.dezzutils.command;

import com.dezzmeister.dezzutils.command.impl.BanCommand;
import com.dezzmeister.dezzutils.command.impl.FindCommand;
import com.dezzmeister.dezzutils.command.impl.GSayCommand;
import com.dezzmeister.dezzutils.command.impl.GTPCommand;
import com.dezzmeister.dezzutils.command.impl.GhostCommand;
import com.dezzmeister.dezzutils.command.impl.KickCommand;
import com.dezzmeister.dezzutils.command.impl.KillCommand;
import com.dezzmeister.dezzutils.command.impl.TPCommand;
import com.dezzmeister.dezzutils.command.impl.UnGhostCommand;

import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class RegisterCommandsEventListener {
	
	@SubscribeEvent
	public void registerCommands(final RegisterCommandsEvent event) {
		// Overidden commands
		TPCommand.register(event.getDispatcher());
		KickCommand.register(event.getDispatcher());
		BanCommand.register(event.getDispatcher());
		KillCommand.register(event.getDispatcher());
		
		// New commands
		GSayCommand.register(event.getDispatcher());
		GhostCommand.register(event.getDispatcher());
		UnGhostCommand.register(event.getDispatcher());
		FindCommand.register(event.getDispatcher());
		GTPCommand.register(event.getDispatcher());
	}
}
