package com.virtual_hotel_agent.search.models.expedia;

import org.json.JSONObject;



public class ValueAdd {

	private int mId;
	public String mDescription;

	public ValueAdd(JSONObject jValueAdd) {
		mId = XpediaDatabase.getSafeInt(jValueAdd,"@id");
		mDescription = XpediaDatabase.getSafeString(jValueAdd,"description");
	}

}
