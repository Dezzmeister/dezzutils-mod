package com.dezzmeister.dezzutils.command;

import com.dezzmeister.dezzutils.command.impl.BanCommand;
import com.dezzmeister.dezzutils.command.impl.GSayCommand;
import com.dezzmeister.dezzutils.command.impl.KickCommand;
import com.dezzmeister.dezzutils.command.impl.KillCommand;
import com.dezzmeister.dezzutils.command.impl.TPCommand;

import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class RegisterCommandsEventListener {
	
	@SubscribeEvent
	public void registerCommands(final RegisterCommandsEvent event) {
		TPCommand.register(event.getDispatcher());
		KickCommand.register(event.getDispatcher());
		BanCommand.register(event.getDispatcher());
		KillCommand.register(event.getDispatcher());
		GSayCommand.register(event.getDispatcher());
	}
}
