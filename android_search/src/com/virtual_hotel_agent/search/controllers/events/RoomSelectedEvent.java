package com.virtual_hotel_agent.search.controllers.events;

import com.virtual_hotel_agent.search.models.expedia.HotelData;
import com.virtual_hotel_agent.search.models.expedia.RoomDetails;

public class RoomSelectedEvent {
	public HotelData hotel;
	public RoomDetails room;

	public RoomSelectedEvent(RoomDetails room, HotelData hotel) {
		this.room = room;
		this.hotel = hotel;
	}

}
