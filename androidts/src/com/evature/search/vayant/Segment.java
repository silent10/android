package com.evature.search.vayant;

import java.util.ArrayList;
import java.util.List;

public class Segment {

	public List<Flight> flights = new ArrayList<Flight>();

	public Segment() {
		// TODO Auto-generated constructor stub
	}

	public void addFlight(Flight flight) {
		flights.add(flight);

	}

	public boolean isDirect() {
		return flights.size() == 1;
	}
}
