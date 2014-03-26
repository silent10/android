package com.virtual_hotel_agent.search.controllers.events;

import com.ean.mobile.hotel.HotelRoom;

/**
 * Room was selected 
 */
public class RoomSelectedEvent {
	public long hotelId;
	public HotelRoom room;

	public RoomSelectedEvent(HotelRoom room, long hotelId) {
		this.room = room;
		this.hotelId = hotelId;
	}

}
