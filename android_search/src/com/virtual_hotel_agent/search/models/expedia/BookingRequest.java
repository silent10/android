package com.virtual_hotel_agent.search.models.expedia;

import java.util.List;


// see http://developer.ean.com/docs/book-reservation/
public class BookingRequest {

	// Common for both Expedia Collect and Hotel Collect
    long hotelId;
    String arrivalDate;
    String departureDate;
    String supplierType; 
    //List<ReservationRoom> roomGroup;
	
	String email;
	String firstName;
	String lastName;
    String homePhone;
    String workPhone;
    
    String bookingEmail;
	String bookingFirstName;
	String bookingLastName;
    String bookingHomePhone;
    String bookingWorkPhone;
    String creditCardType;
    String creditCardNumber;
    String creditCardIdentifier;
    String creditCardExpirationDate;
	
    
	// Expedia Collect only  (if supplierType = "E")
	
	
	
	
	// Hotel Collect only   (if supplierType != "E")
	
	
}
