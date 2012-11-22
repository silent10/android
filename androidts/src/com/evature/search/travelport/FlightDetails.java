package com.evature.search.travelport;

import org.w3c.dom.Element;

public class FlightDetails {
	public String Key;
	public String Origin;
	public String Destination;
	public String DepartureTime;
	public String ArrivalTime;
	public String FlightTime;
	public String TravelTime;
	public String Equipment;
	public String OriginTerminal;
	public String DestinationTerminal;

	public FlightDetails(Element e) {
		Key = e.getAttribute("Key");
		Origin = e.getAttribute("Origin");
		Destination = e.getAttribute("Destination");
		DepartureTime = e.getAttribute("DepartureTime");
		ArrivalTime = e.getAttribute("ArrivalTime");
		FlightTime = e.getAttribute("FlightTime");
		TravelTime = e.getAttribute("TravelTime");
		Equipment = e.getAttribute("Equipment");
		OriginTerminal = e.getAttribute("OriginTerminal");
		DestinationTerminal = e.getAttribute("DestinationTerminal");
	}

}
