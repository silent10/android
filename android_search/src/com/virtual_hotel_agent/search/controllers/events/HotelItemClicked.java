package com.virtual_hotel_agent.search.controllers.events;

import android.view.View;

/****
 * At the hotel list page (or map page) - an hotel was selected
 */

public class HotelItemClicked {

	public int hotelIndex;
	public long hotelId;
	public View hotelName;
	//public View hotelTripAdvRating;
	public View hotelStarRating;

	/**
	 * @param hotelIndex - index in list of hotels
	 */
	public HotelItemClicked(int hotelIndex, long hotelId, View hotelName,/* View hotelTripAdvRating,*/ View hotelStarRating) {
		this.hotelIndex = hotelIndex;
		this.hotelId = hotelId;
		this.hotelName = hotelName;
		//this.hotelTripAdvRating = hotelTripAdvRating;
		this.hotelStarRating = hotelStarRating;
	}

}
