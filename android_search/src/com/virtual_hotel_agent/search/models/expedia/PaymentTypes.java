package com.virtual_hotel_agent.search.models.expedia;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.virtual_hotel_agent.search.controllers.activities.MainActivity;

public class PaymentTypes {

	class PaymentType {
		public String code;
		public String name;
		
		public PaymentType(String code, String name) {
			this.code = code;
			this.name = name;
		}
	}

	private static final String TAG = "PaymentTypes";
	
	PaymentType[] paymentTypes;
	
	public PaymentTypes(JSONObject jObj) {
		JSONObject jPaymentResponse;
		try {
			jPaymentResponse = jObj.getJSONObject("HotelPaymentResponse");
		
			JSONArray jPaymentTypes = jPaymentResponse.getJSONArray("PaymentType");
			int length = jPaymentTypes.length();
			paymentTypes = new PaymentType[length];
			for (int i=0; i<length; i++) {
				JSONObject jPaymentType = jPaymentTypes.getJSONObject(i);
				paymentTypes[i] = new PaymentType(jPaymentType.getString("code"), jPaymentType.getString("name"));
			}
		} catch (JSONException e) {
			MainActivity.LogError(TAG, "Exception loading PaymentTypes", e);
		}
	}
}
