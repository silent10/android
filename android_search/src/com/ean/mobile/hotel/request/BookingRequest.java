/*
 * Copyright (c) 2013, Expedia Affiliate Network
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that redistributions of source code
 * retain the above copyright notice, these conditions, and the following
 * disclaimer. 
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are those
 * of the authors and should not be interpreted as representing official policies, 
 * either expressed or implied, of the Expedia Affiliate Network or Expedia Inc.
 */

package com.ean.mobile.hotel.request;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import org.joda.time.LocalDate;
import org.joda.time.YearMonth;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONException;
import org.json.JSONObject;

import com.ean.mobile.Address;
import com.ean.mobile.Individual;
import com.ean.mobile.exception.EanWsError;
import com.ean.mobile.hotel.Reservation;
import com.ean.mobile.hotel.ReservationRoom;
import com.ean.mobile.hotel.SupplierType;
import com.ean.mobile.request.CommonParameters;
import com.ean.mobile.request.Request;

/**
 * The class that performs booking requests.
 */
public final class BookingRequest extends Request<Reservation> {

    /**
     * This method actually performs the booking request with the passed room and occupancy information.
     * @param hotelId The ID of the hotel to be booked.
     * @param arrivalDate The day that the booking should begin (checkin).
     * @param departureDate The day that the booking will end (checkout).
     * @param supplierType The supplierType (found in the list call, usually "E").
     * @param room The room and its occupancy that will be booked.
     * @param reservationInformation The information about the entity making the reservation. "Billing" information.
     * @param address The address associated with the reservationInformation.
     */
    public BookingRequest(final Long hotelId, final LocalDate arrivalDate, final LocalDate departureDate,
              final SupplierType supplierType, final ReservationRoom room,
              final ReservationInformation reservationInformation, final Address address) {
        this(hotelId, arrivalDate, departureDate, supplierType, Collections.singletonList(room),
                reservationInformation, address);
    }

    /**
     * This method actually performs the booking request with the passed room and occupancy information.
     * @param hotelId The ID of the hotel to be booked.
     * @param arrivalDate The day that the booking should begin (checkin).
     * @param departureDate The day that the booking will end (checkout).
     * @param supplierType The supplierType (found in the list call, usually "E").
     * @param roomGroup The Rooms and their occupancies that will be booked.
     * @param reservationInformation The information about the entity making the reservation. "Billing" information.
     * @param address The address associated with the reservationInformation.
     */
    public BookingRequest(final Long hotelId, final LocalDate arrivalDate, final LocalDate departureDate,
            final SupplierType supplierType, final List<ReservationRoom> roomGroup,
            final ReservationInformation reservationInformation, final Address address) {
        this(hotelId, arrivalDate, departureDate, supplierType, roomGroup, reservationInformation, address, null);
    }

    /**
     * This method actually performs the booking request with the passed room and occupancy information.
     * @param hotelId The ID of the hotel to be booked.
     * @param arrivalDate The day that the booking should begin (checkin).
     * @param departureDate The day that the booking will end (checkout).
     * @param supplierType The supplierType (found in the list call, usually "E").
     * @param roomGroup The Rooms and their occupancies that will be booked.
     * @param reservationInformation The information about the entity making the reservation. "Billing" information.
     * @param address The address associated with the reservationInformation.
     * @param extraBookingData Any extra parameters (like confirmation extra, etc.) to pass to the booking request.
     */
    public BookingRequest(final Long hotelId, final LocalDate arrivalDate,
            final LocalDate departureDate, final SupplierType supplierType, final List<ReservationRoom> roomGroup,
            final ReservationInformation reservationInformation, final Address address,
            final List<NameValuePair> extraBookingData) {

        final List<NameValuePair> rateInformationParameters = Arrays.<NameValuePair>asList(
            new BasicNameValuePair("hotelId", hotelId.toString()),
            new BasicNameValuePair("supplierType", supplierType.code)
        );

        final List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
        urlParameters.addAll(getBasicUrlParameters(arrivalDate, departureDate));
        urlParameters.addAll(rateInformationParameters);
        urlParameters.addAll(ReservationRoom.asNameValuePairs(roomGroup));
        urlParameters.addAll(reservationInformation.asNameValuePairs());
        urlParameters.addAll(address.asBookingRequestPairs());
        urlParameters.addAll(extraBookingData == null ? Collections.<NameValuePair>emptyList() : extraBookingData);

        setUrlParameters(urlParameters);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Reservation consume(final JSONObject jsonObject) throws JSONException, EanWsError {
        if (jsonObject == null) {
            return null;
        }

        final JSONObject response = jsonObject.getJSONObject("HotelRoomReservationResponse");

        if (response.has("EanWsError")) {
            throw EanWsError.fromJson(response.getJSONObject("EanWsError"));
        }

        CommonParameters.customerSessionId = response.optString("customerSessionId");

        return new Reservation(response);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public URI getUri() throws URISyntaxException {
        return new URI("https", "book.api.ean.com", "/ean-services/rs/hotel/v3/res", getQueryString(), null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean requiresSecure() {
        return true;
    }

    /**
     * This is a holder for all of the information required as "billing" information in the booking process.
     */
    public static final class ReservationInformation {

        /**
         * The individual for which this reservation will be made.
         */
        public final Individual individual;

        /**
         * The credit card information to be used by the individual.
         */
        public final CreditCardInformation creditCardInformation;

        /**
         * The main constructor for a reservation information object.
         * @param email The email of the individual.
         * @param firstName The first name of the individual.
         * @param lastName The last name of the individual.
         * @param homePhone The home telephone number of the individual.
         * @param workPhone The work telephone number of the individual.
         * @param creditCardType The credit card type for the booking.
         * @param creditCardNumber The credit card number for the booking.
         * @param creditCardIdentifier The credit card identifier (CCID, CID, etc.)
         * @param creditCardExpirationDate The expiration date of the credit card.
         */
        public ReservationInformation(final String email, final String firstName, final String lastName,
                final String homePhone, final String workPhone, final String creditCardType,
                final String creditCardNumber, final String creditCardIdentifier,
                final YearMonth creditCardExpirationDate) {
            this.individual = new BookingIndividual(email, firstName, lastName, homePhone, workPhone);
            this.creditCardInformation = new CreditCardInformation(
                creditCardType, creditCardNumber, creditCardIdentifier, creditCardExpirationDate);
        }

        /**
         * Gets NameValuePairs for the reservation information so it can be sent in a rest request.
         * @return The requested NameValuePairs
         */
        private List<NameValuePair> asNameValuePairs() {

            final List<NameValuePair> out = new ArrayList<NameValuePair>();
            out.addAll(individual.asNameValuePairs());
            out.addAll(creditCardInformation.asNameValuePairs());

            return Collections.unmodifiableList(out);
        }
    }

    /**
     * Data holder for information about a particular individual.
     * DO NOT SERIALIZE OR SAVE ANYWHERE.
     */
    public static final class BookingIndividual extends Individual {
        /**
         * The constructor for the holder for information about a particular individual.
         * @param email The individual's email.
         * @param firstName The individual's first name.
         * @param lastName The individual's last name.
         * @param homePhone The individual's home telephone number.
         * @param workPhone The individual's work telephone number.
         */
        public BookingIndividual(final String email, final String firstName, final String lastName,
                final String homePhone, final String workPhone) {
            super(email, firstName, lastName, homePhone, workPhone);
        }
    }

    /**
     * A holder for particular information about a credit card used for booking.
     * DO NOT SERIALIZE OR SAVE ANYWHERE.
     */
    public static final class CreditCardInformation {

        /**
         * The formatter for the month field passed to the request.
         */
        private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormat.forPattern("MM");

        /**
         * The formatter for the year field passed to the request.
         */
        private static final DateTimeFormatter YEAR_FORMATTER = DateTimeFormat.forPattern("yyyy");

        /**
         * The type of credit card to be used (MC, VS, etc.).
         */
        public final String type;

        /**
         * The actual credit card number.
         */
        public final String number;

        /**
         * The cid of the credit card.
         */
        public final String identifier;

        /**
         * The card's expiration date, to be formatted later on.
         */
        public final YearMonth expirationDate;

        /**
         * The sole constructor for the holder for credit card information.
         * @param type The credit card's type, see {@link CreditCardInformation#type}.
         * @param number The credit card's number.
         * @param identifier The credit card's identifier.
         * @param expirationDate The credit card's expiration date.
         */
        public CreditCardInformation(
                final String type, final String number, final String identifier, final YearMonth expirationDate) {
            this.type = type;
            this.number = number;
            this.identifier = identifier;
            this.expirationDate = expirationDate;
        }

        /**
         * Gets the list of name value pairs representing this credit card for a booking request.
         * @return The list as mentioned above.
         */
        public List<NameValuePair> asNameValuePairs() {
            return Arrays.<NameValuePair>asList(
                new BasicNameValuePair("creditCardType", type),
                new BasicNameValuePair("creditCardNumber", number),
                new BasicNameValuePair("creditCardIdentifier", identifier),
                new BasicNameValuePair("creditCardExpirationMonth", MONTH_FORMATTER.print(expirationDate)),
                new BasicNameValuePair("creditCardExpirationYear", YEAR_FORMATTER.print(expirationDate)));
        }
    }

}