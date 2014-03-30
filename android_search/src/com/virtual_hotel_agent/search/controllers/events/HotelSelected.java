package com.virtual_hotel_agent.search.controllers.events;

/****
 * At the hotel details page - the "select" button was pressed
 */
public class HotelSelected {

	public long hotelId;
	
	public HotelSelected(long hotelId) {
		this.hotelId = hotelId;
	}

}
