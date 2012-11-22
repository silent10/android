package com.evature.search.travelport;

import org.w3c.dom.Element;

public class AirSegment {
	public String Key;
	public String Group;
	public String Carrier;
	public String FlightNumber;
	public String Origin;
	public String Destination;
	public String DepartureTime;
	public String ArrivalTime;
	public String FlightTime;
	public String TravelTime;
	public String Distance;
	public String ETicketability;
	public String Equipment;
	public String ChangeOfPlane;
	public String ParticipantLevel;
	public String LinkAvailability;
	public String PolledAvailabilityOption;
	public String OptionalServicesIndicator;
	public String AvailabilitySource;

	public AirSegment(Element e) {
		Key = e.getAttribute("Key");
		Group = e.getAttribute("Group");
		Carrier = e.getAttribute("Carrier");
		FlightNumber = e.getAttribute("FlightNumber");
		Origin = e.getAttribute("Origin");
		Destination = e.getAttribute("Destination");
		DepartureTime = e.getAttribute("DepartureTime");
		ArrivalTime = e.getAttribute("ArrivalTime");
		FlightTime = e.getAttribute("FlightTime");
		TravelTime = e.getAttribute("TravelTime");
		Distance = e.getAttribute("Distance");
		ETicketability = e.getAttribute("ETicketability");
		Equipment = e.getAttribute("Equipment");
		ChangeOfPlane = e.getAttribute("ChangeOfPlane");
		ParticipantLevel = e.getAttribute("ParticipantLevel");
		LinkAvailability = e.getAttribute("LinkAvailability");
		PolledAvailabilityOption = e.getAttribute("PolledAvailabilityOption");
		OptionalServicesIndicator = e.getAttribute("OptionalServicesIndicator");
		AvailabilitySource = e.getAttribute("AvailabilitySource");
	}

}
