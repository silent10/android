package com.virtual_hotel_agent.search.controllers.events;


public class HotelItemClicked {

	public int hotelIndex;
	
	public HotelItemClicked(int item) {
		hotelIndex = item;
	}

}
