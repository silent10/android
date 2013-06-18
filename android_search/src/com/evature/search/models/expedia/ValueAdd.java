package com.evature.search.models.expedia;

import org.json.JSONObject;



public class ValueAdd {

	private int mId;
	private String mDescription;

	public ValueAdd(JSONObject jValueAdd) {
		mId = EvaXpediaDatabase.getSafeInt(jValueAdd,"@id");
		mDescription = EvaXpediaDatabase.getSafeString(jValueAdd,"description");
	}

}
