package com.evature.search.models.travelport;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class RailJourney {

	public String Key;
	public String Origin;
	public String Destination;
	public String OriginStationName;
	public String DestinationStationName;
	public String DepartureTime;
	public Date departureTime;
	public String ArrivalTime;
	public Date arrivalTime;
	public String ProviderCode;
	public String SupplierCode;
	public String RailSegmentRef;
	public RailSegment railsegment;
	public List<RailPricingSolution> prices = new ArrayList<RailPricingSolution>();

	public RailJourney(Element e) {
		Key = e.getAttribute("Key");
		Origin = e.getAttribute("Origin");
		Destination = e.getAttribute("Destination");
		OriginStationName = e.getAttribute("OriginStationName");
		DestinationStationName = e.getAttribute("DestinationStationName");
		// SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ"); - this is with the timezone
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
		DepartureTime = e.getAttribute("DepartureTime");
		ArrivalTime = e.getAttribute("ArrivalTime");
		int colon_pos = DepartureTime.lastIndexOf(':'); // Need to remove the last colon to make it RFC822 compliant
		DepartureTime = DepartureTime.substring(0, colon_pos) + DepartureTime.substring(colon_pos + 1);
		colon_pos = ArrivalTime.lastIndexOf(':'); // Need to remove the last colon to make it RFC822 compliant
		ArrivalTime = ArrivalTime.substring(0, colon_pos) + ArrivalTime.substring(colon_pos + 1);
		try {
			departureTime = dateFormat.parse(DepartureTime);
			arrivalTime = dateFormat.parse(ArrivalTime);
		} catch (ParseException e1) {
			e1.printStackTrace();
		}
		ProviderCode = e.getAttribute("ProviderCode");
		SupplierCode = e.getAttribute("SupplierCode");
		NodeList nl = e.getElementsByTagName("rail:RailSegmentRef");
		Element elem = (Element) nl.item(0);
		RailSegmentRef = elem.getAttribute("Key");
	}
}
