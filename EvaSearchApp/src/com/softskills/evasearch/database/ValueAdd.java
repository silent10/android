package com.softskills.evasearch.database;

import org.json.JSONObject;


public class ValueAdd {

	private int mId;
	private String mDescription;

	public ValueAdd(JSONObject jValueAdd) {
		mId = EvaDatabase.getSafeInt(jValueAdd,"@id");
		mDescription = EvaDatabase.getSafeString(jValueAdd,"description");
	}

}
