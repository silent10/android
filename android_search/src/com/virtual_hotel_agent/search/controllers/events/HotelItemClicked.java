package com.virtual_hotel_agent.search.controllers.events;

/****
 * At the hotel list page (or map page) - an hotel was selected
 */

public class HotelItemClicked {

	public int hotelIndex;

	/**
	 * @param hotelIndex - index in list of hotels
	 */
	public HotelItemClicked(int hotelIndex) {
		this.hotelIndex = hotelIndex;
	}

}
