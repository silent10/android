package com.evature.search.travelport;

import org.w3c.dom.Element;

public class RailFare {
	public String Key;
	public String FareBasis;
	public String CabinClass;
	public String PassengerTypeCode;
	public String Origin;
	public String Destination;
	public String EffectiveDate;
	public String Amount;
	public String FareReference;

	public RailFare(Element e) {
		Key = e.getAttribute("Key");
		FareBasis = e.getAttribute("FareBasis");
		CabinClass = e.getAttribute("CabinClass");
		PassengerTypeCode = e.getAttribute("PassengerTypeCode");
		Origin = e.getAttribute("Origin");
		Destination = e.getAttribute("Destination");
		EffectiveDate = e.getAttribute("EffectiveDate");
		Amount = e.getAttribute("Amount");
		FareReference = e.getAttribute("FareReference");

	}

}
