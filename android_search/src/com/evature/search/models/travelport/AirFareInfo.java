package com.evature.search.models.travelport;

import org.w3c.dom.Element;

public class AirFareInfo {
	public String Key;
	public String FareBasis;
	public String PassengerTypeCode;
	public String Origin;
	public String Destination;
	public String EffectiveDate;
	public String DepartureDate;
	public String Amount;
	public String PrivateFare;
	public String NegotiatedFare;
	public String NotValidBefore;
	public String NotValidAfter;

	public AirFareInfo(Element e) {
		Key = e.getAttribute("Key");
		FareBasis = e.getAttribute("FareBasis");
		PassengerTypeCode = e.getAttribute("PassengerTypeCode");
		Origin = e.getAttribute("Origin");
		Destination = e.getAttribute("Destination");
		EffectiveDate = e.getAttribute("EffectiveDate");
		DepartureDate = e.getAttribute("DepartureDate");
		Amount = e.getAttribute("Amount");
		PrivateFare = e.getAttribute("PrivateFare");
		NegotiatedFare = e.getAttribute("NegotiatedFare");
		NotValidBefore = e.getAttribute("NotValidBefore");
		NotValidAfter = e.getAttribute("NotValidAfter");
	}

}
