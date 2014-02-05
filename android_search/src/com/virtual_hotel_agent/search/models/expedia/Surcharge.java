package com.virtual_hotel_agent.search.models.expedia;

import org.json.JSONObject;



public class Surcharge {

	public double mAmount;
	public String mType;

	public Surcharge(JSONObject jSurcharge) {
		mAmount = XpediaDatabase.getSafeDouble(jSurcharge, "@amount");
		mType = XpediaDatabase.getSafeString(jSurcharge, "@type");
		mType = mType.replaceAll(
			      String.format("%s|%s|%s",
			    	         "(?<=[A-Z])(?=[A-Z][a-z])",
			    	         "(?<=[^A-Z])(?=[A-Z])",
			    	         "(?<=[A-Za-z])(?=[^A-Za-z])"
			    	      ),
			    	      " "
			    	   );
	}

}
