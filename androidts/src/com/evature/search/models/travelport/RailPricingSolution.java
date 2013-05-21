package com.evature.search.models.travelport;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class RailPricingSolution {

	public String Key;
	public String OfferId;
	public String TotalPrice;
	public double totalPrice;
	public String ApproximateTotalPrice;
	public String ProviderCode;
	public String SupplierCode;
	public String RailJourneyRef;
	public String RailFareRef;
	public String PassengerTypeCode; // Todo
	public String PassengerTypeGender; // todo

	public RailPricingSolution(Element e) {
		Key = e.getAttribute("Key");
		OfferId = e.getAttribute("OfferId");
		TotalPrice = e.getAttribute("TotalPrice");
		totalPrice = Double.parseDouble(TotalPrice.substring(3)); // assumes currency is EUR!!!
		ApproximateTotalPrice = e.getAttribute("ApproximateTotalPrice");
		ProviderCode = e.getAttribute("ProviderCode");
		SupplierCode = e.getAttribute("SupplierCode");
		NodeList nl = e.getElementsByTagName("rail:RailJourneyRef");
		Element elem = (Element) nl.item(0);
		RailJourneyRef = elem.getAttribute("Key");
		nl = e.getElementsByTagName("rail:RailPricingInfo");
		elem = (Element) nl.item(0);
		nl = elem.getElementsByTagName("rail:RailBookingInfo");
		elem = (Element) nl.item(0);
		RailFareRef = elem.getAttribute("RailFareRef");
	}

}
