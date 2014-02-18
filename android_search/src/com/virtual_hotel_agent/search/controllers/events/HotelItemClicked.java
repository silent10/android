package com.virtual_hotel_agent.search.controllers.events;

import com.virtual_hotel_agent.search.models.chat.ChatItem;

public class HotelItemClicked {

	public int hotelIndex;
	
	public HotelItemClicked(int item) {
		hotelIndex = item;
	}

}
