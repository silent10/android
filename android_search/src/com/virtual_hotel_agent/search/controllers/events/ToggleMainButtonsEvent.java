package com.virtual_hotel_agent.search.controllers.events;

public class ToggleMainButtonsEvent {

	public boolean showMainButtons;
	
	public ToggleMainButtonsEvent(boolean showMainButtons) {
		this.showMainButtons = showMainButtons;
	}
}
