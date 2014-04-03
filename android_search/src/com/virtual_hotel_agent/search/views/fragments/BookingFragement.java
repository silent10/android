package com.virtual_hotel_agent.search.views.fragments;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Currency;
import java.util.List;
import java.util.UUID;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.joda.time.LocalDate;
import org.joda.time.YearMonth;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import roboguice.event.EventManager;
import roboguice.fragment.RoboFragment;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.ean.mobile.Address;
import com.ean.mobile.exception.EanWsError;
import com.ean.mobile.exception.UrlRedirectionException;
import com.ean.mobile.hotel.Hotel;
import com.ean.mobile.hotel.HotelRoom;
import com.ean.mobile.hotel.NightlyRate;
import com.ean.mobile.hotel.Reservation;
import com.ean.mobile.hotel.ReservationRoom;
import com.ean.mobile.hotel.RoomOccupancy;
import com.ean.mobile.hotel.SupplierType;
import com.ean.mobile.hotel.request.BookingRequest;
import com.ean.mobile.request.RequestProcessor;
import com.evature.util.Log;
import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.GoogleAnalytics;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.Tracker;
import com.google.inject.Inject;
import com.virtual_hotel_agent.search.BuildConfig;
import com.virtual_hotel_agent.search.VHAApplication;
import com.virtual_hotel_agent.search.R;
import com.virtual_hotel_agent.search.controllers.activities.MainActivity;
import com.virtual_hotel_agent.search.controllers.events.BookingCompletedEvent;

public class BookingFragement extends RoboFragment {

	private static final String TAG = "BookingFragement";
	private static final String DATE_FORMAT_STRING = "EEEE, MMMM dd, yyyy";
    private static final String NIGHTLY_RATE_FORMAT_STRING = "MM-dd-yyyy";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormat.forPattern(DATE_FORMAT_STRING);
    private static final DateTimeFormatter NIGHTLY_RATE_FORMATTER
        = DateTimeFormat.forPattern(NIGHTLY_RATE_FORMAT_STRING);
	private static final int PICK_CONTACT_INTENT = 1;
	private ArrayList<String> mCreditCardTypes;
	private ArrayList<String> mCreditCardValues;
	
	@Inject protected EventManager eventManager;
	
	private Hotel hotel;
	private HotelRoom hotelRoom;
	private View mView = null;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		if (mView != null) {
			Log.w(TAG, "Fragment initialized twice");
			((ViewGroup) mView.getParent()).removeView(mView);
			return mView;
		}
		
		
		Context context = BookingFragement.this.getActivity();
		Tracker defaultTracker = GoogleAnalytics.getInstance(context).getDefaultTracker();
		if (defaultTracker != null) 
			defaultTracker.send(MapBuilder
				    .createAppView()
				    .set(Fields.SCREEN_NAME, "Booking fragment")
				    .build()
				);
		
		mView = inflater.inflate(R.layout.fragment_bookingsummary, container, false);
		
		Button chooseContact = (Button) mView.findViewById(R.id.choose_contact_button);
		chooseContact.setOnClickListener(mChooseContact);
		
		Button loadDefault = (Button) mView.findViewById(R.id.button_load_default_billing_info);
		if (BuildConfig.DEBUG) {
			loadDefault.setOnClickListener(mDefaultBilling);
		}
		else {
			loadDefault.setVisibility(View.GONE);
		}
		
		Button completeBooking = (Button) mView.findViewById(R.id.button_complete_booking);
		completeBooking.setOnClickListener(mCompleteBooking);
		
		Button toggleBilling = (Button) mView.findViewById(R.id.button_billing_info);
		toggleBilling.setOnClickListener(mToggleBilling);
		
		Button toggleGuests = (Button) mView.findViewById(R.id.button_show_guests);
		toggleGuests.setOnClickListener(mToggleGuests);
		
        if (VHAApplication.selectedHotel == null || VHAApplication.selectedRoom == null) {
        	VHAApplication.logError(TAG, "Null hotel/room");
        	return mView;
        }

        changeHotelRoom(VHAApplication.selectedHotel, VHAApplication.selectedRoom);
        
        TextView legal = (TextView) mView.findViewById(R.id.legal_text);
        // Expedia collect credit card charged on the spot -
        // TODO: change text for hotel collect
        legal.setText(Html.fromHtml("Your credit card will be charged for the full payment upon submitting your reservation request.<br>\n"
			+"See <a href=\"http://www.travelnow.com/templates/352395/terms-of-use?lang=en&currency=USD&secureUrlFromDataBridge=https://www.travelnow.com&requestVersion=V2&source=g3\">User Agreement</a><br>\n"
			+"We protect your credit card information.\n" 
			+"See <a href=\"http://www.travelnow.com/templates/352395/privacy-policy?lang=en&currency=USD&secureUrlFromDataBridge=https://www.travelnow.com&requestVersion=V2\">Privacy Statement</a>"));
        legal.setMovementMethod(LinkMovementMethod.getInstance());

		return mView;
	}

	private NumberFormat getCurrencyFormat(final String currencyCode) {
        final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();
        currencyFormat.setCurrency(Currency.getInstance(currencyCode));
        return currencyFormat;
    }
	
	private void fillData() {
        final TextView hotelName = (TextView) mView.findViewById(R.id.hotelName);
        final TextView checkIn = (TextView) mView.findViewById(R.id.arrivalDisplay);
        final TextView checkOut = (TextView) mView.findViewById(R.id.departureDisplay);
        final TextView numGuests = (TextView) mView.findViewById(R.id.guestsNumberDisplay);
        final TextView roomType = (TextView) mView.findViewById(R.id.roomTypeDisplay);
        final TextView bedType = (TextView) mView.findViewById(R.id.bedTypeDisplay);
        final TextView taxesAndFees = (TextView) mView.findViewById(R.id.taxes_and_fees_display);
        final TextView totalLowPrice = (TextView) mView.findViewById(R.id.lowPrice);
		
        hotelName.setText(hotel.name);
        
        final RoomOccupancy occupancy = hotelRoom.rate.roomGroup.get(0).occupancy;
        
        checkIn.setText(DATE_TIME_FORMATTER.print(VHAApplication.arrivalDate));
        checkOut.setText(DATE_TIME_FORMATTER.print(VHAApplication.departureDate));
        
        numGuests.setText(getGuestsText(occupancy));
        roomType.setText(hotelRoom.description);
        bedType.setText(hotelRoom.bedTypes.get(0).description);

        final NumberFormat currencyFormat = getCurrencyFormat(hotel.currencyCode);

        taxesAndFees.setText(currencyFormat.format(hotelRoom.getTaxesAndFees()));

        totalLowPrice.setText(currencyFormat.format(hotelRoom.getTotalRate()));

        displayTotalHighPrice(currencyFormat);
        populatePriceBreakdownList(currencyFormat);

	}
	

	private void populatePriceBreakdownList(final NumberFormat currencyFormat) {
        final LinearLayout priceBreakdownList = (LinearLayout) mView.findViewById(R.id.priceDetailsBreakdown);
        View view;
        final LayoutInflater inflater = getLayoutInflater(null);

        priceBreakdownList.removeAllViews();
        
        LocalDate currentDate = VHAApplication.arrivalDate.minusDays(1);
        for (NightlyRate rate : hotelRoom.rate.chargeable.nightlyRates) {
            view = inflater.inflate(R.layout.pricebreakdownlayout, null);
            final TextView date = (TextView) view.findViewById(R.id.priceBreakdownDate);
            final TextView highPrice = (TextView) view.findViewById(R.id.priceBreakdownHighPrice);
            final TextView lowPrice = (TextView) view.findViewById(R.id.priceBreakdownLowPrice);

            currentDate = currentDate.plusDays(1);
            date.setText(NIGHTLY_RATE_FORMATTER.print(currentDate));

            lowPrice.setText(currencyFormat.format(rate.rate));
            if (rate.rate.equals(rate.baseRate)) {
                highPrice.setVisibility(TextView.GONE);
            } else {
                highPrice.setVisibility(TextView.VISIBLE);
                highPrice.setText(currencyFormat.format(rate.baseRate));
                highPrice.setPaintFlags(highPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            }
            priceBreakdownList.addView(view);
        }
    }
	
	private static String getGuestsText(RoomOccupancy occupancy) {
		StringBuilder guests = new StringBuilder();
		int childNum = occupancy.childAges.size();
        if (occupancy.numberOfAdults > 0) {
        	if (occupancy.numberOfAdults > 1) {
        		guests.append(occupancy.numberOfAdults).append(" Adults");
        	}
        	else {
        		guests.append("One Adult");
        	}
        	if (childNum > 0) {
        		guests.append(", ");
        	}
        }
		if (childNum > 0) {
        	if (childNum > 0) {
        		guests.append(childNum).append(" Children");
        	}
        	else {
        		guests.append("One Child"); 
        	}
        }
        return guests.toString();
	}

	private void displayTotalHighPrice(final NumberFormat currencyFormat) {
       final TextView totalHighPrice = (TextView) mView.findViewById(R.id.highPrice);
       //final ImageView drrIcon = (ImageView) mView.findViewById(R.id.drrPromoImg);
       final TextView drrPromoText = (TextView) mView.findViewById(R.id.drrPromoText);

       if (hotelRoom.getTotalRate().equals(hotelRoom.getTotalBaseRate())) {
           // if there's no promo, then we make the promo stuff disappear.
           totalHighPrice.setVisibility(TextView.GONE);
           //drrIcon.setVisibility(ImageView.GONE);
           drrPromoText.setVisibility(ImageView.GONE);
       } else {
           // if there is a promo, we make it show up.
			if (hotelRoom.rate.promoDescription != null
					&& hotelRoom.rate.promoDescription.equals("") == false) {
				drrPromoText.setText(hotelRoom.rate.promoDescription);
				drrPromoText.setVisibility(View.VISIBLE);
			} else {
				drrPromoText.setVisibility(View.GONE);
			}

           //drrPromoText.setText(hotelRoom.promoDescription);
           totalHighPrice.setVisibility(TextView.VISIBLE);
           //drrIcon.setVisibility(ImageView.VISIBLE);
           drrPromoText.setVisibility(ImageView.VISIBLE);
           totalHighPrice.setText(currencyFormat.format(hotelRoom.getTotalBaseRate()));
           totalHighPrice.setPaintFlags(totalHighPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
       }
   }
	
    
	public void changeHotelRoom(Hotel hotel, HotelRoom room) {
		Log.i(TAG, "Setting hotelId to "+hotel.hotelId+ "   room: "+room.description);
		if (this.hotel == hotel && this.hotelRoom == room) {
			return;
		}
		this.hotel = hotel;
		this.hotelRoom = room;
		
		// TODO: credit type options should be taken from EAN response
		mCreditCardTypes = new ArrayList<String>(Arrays.asList(new String[] { "Visa", "MasterCard", "American Express" }));
		mCreditCardValues = new ArrayList<String>(Arrays.asList(new String[] { "VI", "MC", "AE" }));
		if (mView != null) {
			final Spinner cardType = (Spinner) mView.findViewById(R.id.billingInformationCCType);
			ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this.getActivity(), 
					android.R.layout.simple_spinner_dropdown_item, mCreditCardTypes);
			spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			cardType.setAdapter(spinnerArrayAdapter);
		}
		
		fillData();
	}

	
	
	/**
	 * (Event handler) Contains the action to handle the contact choose button.
	 */
	private OnClickListener mChooseContact = new OnClickListener() {
		@Override
		public void onClick(View v) {
	        final Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
	        startActivityForResult(intent, PICK_CONTACT_INTENT);
		}
	};
	
	
	private void showBillingInfo(int show) {
		final View guestsInfo = mView.findViewById(R.id.guestinfolayout);
		final View securityNotice = mView.findViewById(R.id.securityNoticeLayout);
		final View billingInfo = mView.findViewById(R.id.billinginformationlayout);
		
		billingInfo.setVisibility(show);
		securityNotice.setVisibility(show);
		
		guestsInfo.setVisibility(View.GONE);
	}
	
	private OnClickListener mToggleBilling = new OnClickListener() {
		@Override
		public void onClick(View v) {
			final View billingInfo = mView.findViewById(R.id.billinginformationlayout);
			int show = billingInfo.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE;
			showBillingInfo(show);
		}
	};

	private void showGuestInfo(int show) {
		final View guestsInfo = mView.findViewById(R.id.guestinfolayout);
		final View securityNotice = mView.findViewById(R.id.securityNoticeLayout);
		final View billingInfo = mView.findViewById(R.id.billinginformationlayout);
		
		
		guestsInfo.setVisibility(show);
		securityNotice.setVisibility(show);
		
		billingInfo.setVisibility(View.GONE);
	}

	
	private OnClickListener mToggleGuests = new OnClickListener() {
		@Override
		public void onClick(View v) {
			final View guestsInfo = mView.findViewById(R.id.guestinfolayout);
			int show = guestsInfo.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE;
			showGuestInfo(show);
		}
	};
	
	
	@SuppressWarnings("serial")
	static class InvalidInputException extends Exception {
		View offendingView;
		String message;

		public InvalidInputException(View offendingView, String message) {
			this.message = message;
			this.offendingView = offendingView;
		}
		
	}
	
	/***
	 * Check EditText for valid string -
	 * throws InvalidInputException exception in case of error value
	 * returns string  
	 * @throws InvalidInputException 
	 */
	private String validateInput(int redId, int minLen, int maxLen) throws InvalidInputException {
		EditText editText = (EditText) mView.findViewById(redId);
		final String text = editText.getText().toString();
		if (text.length() == 0 && minLen > 0) {
			throw new InvalidInputException(editText, "{} cannot be empty.");
		}
		if (text.length() < minLen) {
			throw new InvalidInputException(editText, "{} should be at least "+ minLen + " characters long.");
		}
		if (text.length() > maxLen) {
			throw new InvalidInputException(editText, "{} should be at most "+ minLen + " characters long.");
		}
		return text;
	}
	
	/**
	 * (Event hanlder) Handles the complete booking button click. Loads the information from the inputs and
	 * creates a new booking request based on that.
	 */
	private OnClickListener mCompleteBooking = new OnClickListener() {
		@Override
		public void onClick(View v) {
			try {
				final String firstName = validateInput(R.id.guestFirstName, 2, 25);
				final String lastName = validateInput(R.id.guestLastName, 2, 40);
				final String phone = validateInput(R.id.guestPhoneNumber, 7, 16);
		        final String email = validateInput(R.id.guestEmail, 6, 40);
	
		        final String addressLine1 = validateInput(R.id.billingInformationAddress1, 1, 100);
		        final String addressLine2 = validateInput(R.id.billingInformationAddress2, -1, 100);
		        final String city = validateInput(R.id.billingInformationCity, 2, 40);
		        final String state = validateInput(R.id.billingInformationState, 2, 30);
		        final String country = validateInput(R.id.billingInformationCountry, 2, 30);
		        final String zip = validateInput(R.id.billingInformationZip, 5, 16);
	
		        final String cardTypeStr = ((Spinner) mView.findViewById(R.id.billingInformationCCType)).getSelectedItem().toString();
		        final String cardType = mCreditCardValues.get(mCreditCardTypes.indexOf(cardTypeStr));
		        final String cardNumber = validateInput(R.id.billingInformationCCNum, 9, 20);
		        final String cardExpirationMonth
		            = ((Spinner) mView.findViewById(R.id.billingInformationCCExpMo)).getSelectedItem().toString();
		        final String cardExpirationYear
		            = ((Spinner) mView.findViewById(R.id.billingInformationCCExpYr)).getSelectedItem().toString();
		        final String cardSecurityCode
		            = validateInput(R.id.billingInformationCCSecurityCode, 3, 6);
	
	
		        final int cardExpirationFullYear = Integer.parseInt(cardExpirationYear);
		        final int cardExpirationFullMonth = Integer.parseInt(cardExpirationMonth);
	
		        final YearMonth expirationDate = new YearMonth(cardExpirationFullYear, cardExpirationFullMonth);
	
		        final BookingRequest.ReservationInformation reservationInfo = new BookingRequest.ReservationInformation(
		            email, firstName, lastName, phone, null, cardType, cardNumber, cardSecurityCode, expirationDate);
	
		        
		        final ReservationRoom reservationRoom = new ReservationRoom(
		            reservationInfo.individual.name,
		            hotelRoom,
		            hotelRoom.bedTypes.get(0).id,
		            VHAApplication.occupancy());
	
		        final Address reservationAddress
		            = new Address(Arrays.asList(addressLine1, addressLine2), city, state, country, zip);
	
		        final List<NameValuePair> extraParameters = Arrays.<NameValuePair>asList(
		                new BasicNameValuePair("sendReservationEmail", "true"),
		                new BasicNameValuePair("affiliateCustomerId", UUID.randomUUID().toString())
		            );

		        
		        final BookingRequest request = new BookingRequest(
	        		hotel.hotelId,
	        		VHAApplication.arrivalDate,
	        		VHAApplication.departureDate,
	        		hotel.supplierType,
		            Collections.singletonList(reservationRoom),
		            reservationInfo,
		            reservationAddress, 
		            extraParameters);
	
		        
		        final NumberFormat currencyFormat = getCurrencyFormat(hotel.currencyCode);
		        
		        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			    builder.setMessage("Are you sure?\nYour card will be charged "+currencyFormat.format(hotelRoom.getTotalRate()))
			            .setTitle("Book Hotel");
			    builder.setNegativeButton(R.string.cancel, null);
			    builder.setPositiveButton("Purchase", new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id) {
			               // User clicked OK button
	
				        	Toast.makeText(getActivity(), "Booking...", Toast.LENGTH_LONG).show();
					        new BookingRequestTask().execute(request);
			           }
			       });
	
			    AlertDialog dialog = builder.create();
			    dialog.show();
	        

			} catch (InvalidInputException e) {
				View offending = e.offendingView;
				String message = e.message;
				if (offending instanceof EditText) {
					EditText et = (EditText) offending;
					message = message.replace("{}", et.getHint());
				}
				Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
				
				// climb up from offending view until reach containr - show that container and focus view
				ViewParent parent = offending.getParent() ;
				while (parent != null) {
					if (parent instanceof View == false) {
						break;
					}
					View vParent = (View) parent;
					if (vParent.getId() == R.id.guestinfolayout) {
						showGuestInfo(View.VISIBLE);
						break;
					}
					if (vParent.getId() == R.id.billinginformationlayout) {
						showBillingInfo(View.VISIBLE);
						break;
					}
					parent = parent.getParent();
				}
				offending.requestFocus();
			}
	    }
	};
	
	/**
     * The task used to actually perform the booking request and pass the returned data off to the next activity.
     */
    private class BookingRequestTask extends AsyncTask<BookingRequest, Void, Boolean> {
        @Override
        protected Boolean doInBackground(final BookingRequest... bookingRequests) {
        	Tracker defaultTracker = GoogleAnalytics.getInstance(BookingFragement.this.getActivity()).getDefaultTracker();
			if (defaultTracker != null) 
				defaultTracker.send(MapBuilder
					    .createEvent("booking", "booking_start", "", hotel.hotelId)
					    .build()
					   );
            for (BookingRequest request : bookingRequests) {
                try {
                    final Reservation reservation = RequestProcessor.run(request);
                    VHAApplication.addReservationToCache(reservation);
                    
                    if (defaultTracker != null) 
        				defaultTracker.send(MapBuilder
        					    .createEvent("booking", "booking_completed", "", hotel.hotelId)
        					    .build()
        					   );
                } catch (EanWsError ewe) {
                    VHAApplication.logError(TAG, "An APILevel Exception occurred.", ewe);
                    return Boolean.FALSE;
                } catch (UrlRedirectionException  ure) {
                    VHAApplication.sendRedirectionToast();
                    return Boolean.FALSE;
                }
            }
            return Boolean.TRUE;
        }
        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);
            
            if (!success ) { 
            	Toast errorToast = Toast.makeText(BookingFragement.this.getActivity(), "There was an error, please try again later.", Toast.LENGTH_LONG);
            	errorToast.show();
            }
            else {
	            //startActivity(new Intent(getActivity(), ReservationDisplayFragment.class));
	            eventManager.fire(new BookingCompletedEvent());
            }
        }
    }
	
	/**
	 * (Event handler) Contains the action to handle the load default billing info button.
	 */
	private OnClickListener mDefaultBilling = new OnClickListener() {
		@Override
		public void onClick(View v) {
			//
			// http://developer.ean.com/docs/test-booking-procedures/
			// For Expedia Collect Static Tests:
			// firstName: Test Booking
			// lastName: Test Booking
			// creditCardType: MC (MasterCard)
			// creditCardNumber: 5401999999999999
			// creditCardIdentifier: 123
			// creditCardExpirationMonth and creditCardExpirationYear: Any date
			// after the reservation
			// address1: travelnow (must be lowercase)
			//
			// For Hotel Collect Static Tests:
			// firstName: Test Booking
			// lastName: Test Booking
			// creditCardType: VI (Visa)
			// creditCardNumber: 4005550000000019
			// creditCardIdentifier: 123
			// creditCardExpirationMonth and creditCardExpirationYear: Any date
			// after the reservation
			// address1: travelnow (must be lowercase)

			// TODO: load from file

			final EditText firstName = (EditText) mView
					.findViewById(R.id.guestFirstName);
			final EditText lastName = (EditText) mView
					.findViewById(R.id.guestLastName);
			final EditText phone = (EditText) mView
					.findViewById(R.id.guestPhoneNumber);
			final EditText email = (EditText) mView
					.findViewById(R.id.guestEmail);

			final EditText addressLine1 = (EditText) mView
					.findViewById(R.id.billingInformationAddress1);
			final EditText addressLine2 = (EditText) mView
					.findViewById(R.id.billingInformationAddress2);
			final EditText city = (EditText) mView
					.findViewById(R.id.billingInformationCity);
			final EditText state = (EditText) mView
					.findViewById(R.id.billingInformationState);
			final EditText country = (EditText) mView
					.findViewById(R.id.billingInformationCountry);
			final EditText zip = (EditText) mView
					.findViewById(R.id.billingInformationZip);

			final Spinner cardType = (Spinner) mView
					.findViewById(R.id.billingInformationCCType);
			final EditText cardNum = (EditText) mView
					.findViewById(R.id.billingInformationCCNum);
			final Spinner cardExpirationMonth = (Spinner) mView
					.findViewById(R.id.billingInformationCCExpMo);
			final Spinner cardExpirationYear = (Spinner) mView
					.findViewById(R.id.billingInformationCCExpYr);
			final EditText cardSecurityCode = (EditText) mView
					.findViewById(R.id.billingInformationCCSecurityCode);

			firstName.setText("Test Booking");
			lastName.setText("Test Booking");
			phone.setText("123456789");
			email.setText("iftah@evature.com");

			addressLine1.setText("travelnow");
			addressLine2.setText("");
			city.setText("Seattle");
			state.setText("WA");
			country.setText("US");
			zip.setText("98004");
			if (hotel.supplierType == SupplierType.EXPEDIA) {
				cardType.setSelection(mCreditCardValues.indexOf("MC"));
				cardNum.setText("5401999999999999");
				// creditCardType: MC (MasterCard)
				// creditCardNumber: 5401999999999999

			} else {
				cardType.setSelection(mCreditCardValues.indexOf("VI"));
				cardNum.setText("4005550000000019");
			}

			cardExpirationMonth.setSelection(Arrays.asList(
					getResources().getStringArray(R.array.credit_card_months))
					.indexOf("01"));
			cardExpirationYear.setSelection(Arrays.asList(
					getResources().getStringArray(R.array.credit_card_years))
					.indexOf("2015"));// Integer.toString((YearMonth.now().getYear()
										// + 1) % yearsInACentury));
			cardSecurityCode.setText("123");
		}
	};


}
