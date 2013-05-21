package com.evature.search.models.travelport;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class AirLowFareSearchRsp {

	public Map<String, RailJourney> railJourneys = new HashMap<String, RailJourney>(); // This is the main element
	public Map<String, RailSegment> railSegments = new HashMap<String, RailSegment>();
	public Map<String, RailFare> railFares = new HashMap<String, RailFare>();
	public Map<String, RailPricingSolution> railPricingsolutions = new HashMap<String, RailPricingSolution>();
	public List<RailJourney> railJourneyList;
	public Map<String, FlightDetails> flightDetails = new HashMap<String, FlightDetails>();
	public Map<String, AirSegment> airSegments = new HashMap<String, AirSegment>();
	public Map<String, AirFareInfo> airfareInfos = new HashMap<String, AirFareInfo>();
	public List<AirPricingSolution> airPricingSolutions = new ArrayList<AirPricingSolution>();

	public AirLowFareSearchRsp(Document document) {
		System.out.println("CTOR");
		NodeList nl;
		nl = document.getElementsByTagName("air:FareInfo");
		for (int i = 0; i < nl.getLength(); i++) { // looping through all air fare infos
			Element e = (Element) nl.item(i);
			AirFareInfo airfare = new AirFareInfo(e);
			airfareInfos.put(airfare.Key, airfare);
		}
		System.out.println("found " + nl.getLength() + " air:FareInfo");
		nl = document.getElementsByTagName("air:AirSegment");
		for (int i = 0; i < nl.getLength(); i++) { // looping through all air segments
			Element e = (Element) nl.item(i);
			AirSegment seg = new AirSegment(e);
			airSegments.put(seg.Key, seg);
		}
		System.out.println("found " + nl.getLength() + " air:AirSegment");
		nl = document.getElementsByTagName("air:AirPricingSolution");
		for (int i = 0; i < nl.getLength(); i++) { // looping through all air pricing solutions
			Element e = (Element) nl.item(i);
			AirPricingSolution airPricingSolution = new AirPricingSolution(e, airSegments);
			airPricingSolutions.add(airPricingSolution);
		}
		System.out.println("found " + nl.getLength() + " air:AirPricingSolution");
		nl = document.getElementsByTagName("air:FlightDetails");
		for (int i = 0; i < nl.getLength(); i++) { // looping through all flights details
			Element e = (Element) nl.item(i);
			FlightDetails detail = new FlightDetails(e);
			flightDetails.put(detail.Key, detail);
		}
		System.out.println("found " + nl.getLength() + " air:FlightDetails");
		nl = document.getElementsByTagName("rail:RailSegment");
		for (int i = 0; i < nl.getLength(); i++) { // looping through all rail segments
			Element e = (Element) nl.item(i);
			RailSegment seg = new RailSegment(e);
			railSegments.put(seg.Key, seg);
		}
		System.out.println("found " + nl.getLength() + " rail:RailSegment");
		nl = document.getElementsByTagName("rail:RailJourney");
		for (int i = 0; i < nl.getLength(); i++) { // looping through all rail journeys
			Element e = (Element) nl.item(i);
			RailJourney journey = new RailJourney(e);
			journey.railsegment = railSegments.get(journey.RailSegmentRef);
			railJourneys.put(journey.Key, journey);
		}
		System.out.println("found " + nl.getLength() + " rail:RailJourney");
		nl = document.getElementsByTagName("rail:RailFare");
		for (int i = 0; i < nl.getLength(); i++) { // looping through all rail fares
			Element e = (Element) nl.item(i);
			RailFare fare = new RailFare(e);
			railFares.put(fare.Key, fare);
		}
		System.out.println("found " + nl.getLength() + " rail:RailFare");
		nl = document.getElementsByTagName("rail:RailPricingSolution");
		for (int i = 0; i < nl.getLength(); i++) { // looping through all rail pricing solutions
			Element e = (Element) nl.item(i);
			RailPricingSolution price = new RailPricingSolution(e);
			// price.railsegment = railSegments.get(price.RailSegmentRef);
			railPricingsolutions.put(price.Key, price);
			railJourneys.get(price.RailJourneyRef).prices.add(price);
		}
		System.out.println("found " + nl.getLength() + " rail:RailPricingSolution");
		railJourneyList = new ArrayList<RailJourney>(railJourneys.values()); // This is the main element as a list

	}

}
