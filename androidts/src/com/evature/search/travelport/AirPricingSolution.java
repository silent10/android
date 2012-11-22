package com.evature.search.travelport;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class AirPricingSolution {
	public String Key;
	public String TotalPrice;
	public Double totalPrice;
	public String Currency;
	public String BasePrice;
	public String ApproximateTotalPrice;
	public String ApproximateBasePrice;
	public String EquivalentBasePrice;
	public String Taxes;
	public List<AirSegment> segments = new ArrayList<AirSegment>();

	public AirPricingSolution(Element e, Map<String, AirSegment> airSegments) {
		Key = e.getAttribute("Key");
		TotalPrice = e.getAttribute("TotalPrice");
		totalPrice = Double.parseDouble(TotalPrice.substring(3)); // assumes currency is EUR!!!
		Currency = TotalPrice.substring(0, 3);
		BasePrice = e.getAttribute("BasePrice");
		ApproximateTotalPrice = e.getAttribute("ApproximateTotalPrice");
		ApproximateBasePrice = e.getAttribute("ApproximateBasePrice");
		EquivalentBasePrice = e.getAttribute("EquivalentBasePrice");
		Taxes = e.getAttribute("Taxes");
		NodeList nl = e.getElementsByTagName("air:AirSegmentRef");
		for (int i = 0; i < nl.getLength(); i++) { // looping through all AirSegmentRefs
			Element ee = (Element) nl.item(i);
			String segmentKey = ee.getAttribute("Key");
			segments.add(airSegments.get(segmentKey));
		}

	}
}
