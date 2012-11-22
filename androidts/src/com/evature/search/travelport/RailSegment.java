package com.evature.search.travelport;

import org.w3c.dom.Element;

public class RailSegment {

	public String Key;
	public String TrainNumber;
	public String Destination;
	public String DepartureTime;
	public String ArrivalTime;
	public String OriginStationName;
	public String DestinationStationName;
	public String TravelTime;
	public String OperatingCompany;

	public RailSegment(Element e) {
		Key = e.getAttribute("Key");
		TrainNumber = e.getAttribute("TrainNumber");
		Destination = e.getAttribute("Destination");
		DepartureTime = e.getAttribute("DepartureTime");
		ArrivalTime = e.getAttribute("ArrivalTime");
		OriginStationName = e.getAttribute("OriginStationName");
		DestinationStationName = e.getAttribute("DestinationStationName");
		TravelTime = e.getAttribute("TravelTime");
		XMLParser parser = new XMLParser();
		OperatingCompany = parser.getValue(e, "rail:OperatingCompany");

	}
}
