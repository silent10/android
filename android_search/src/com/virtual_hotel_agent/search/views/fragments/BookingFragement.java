package com.virtual_hotel_agent.search.views.fragments;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Currency;

import roboguice.fragment.RoboFragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.evature.util.Log;
import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.GoogleAnalytics;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.Tracker;
import com.virtual_hotel_agent.search.MyApplication;
import com.virtual_hotel_agent.search.R;
import com.virtual_hotel_agent.search.models.expedia.ExpediaAppState;
import com.virtual_hotel_agent.search.models.expedia.HotelData;
import com.virtual_hotel_agent.search.models.expedia.RoomDetails;

public class BookingFragement extends RoboFragment {

	private static final String TAG = "BookingFragement";
	private static final int PICK_CONTACT_INTENT = 1;
	private View mView = null;
	private HotelData mHotel;
	private RoomDetails mRoom;
	private ArrayList<String> mCreditCardTypes;
	

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
		loadDefault.setOnClickListener(mDefaultBilling);
		
		Button completeBooking = (Button) mView.findViewById(R.id.button_complete_booking);
		completeBooking.setOnClickListener(mCompleteBooking);
		
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
        final TextView taxesAndFees = (TextView) mView.findViewById(R.id.taxes_and_fees_display);
        final TextView totalLowPrice = (TextView) mView.findViewById(R.id.lowPrice);
		
        ExpediaAppState expediaAppState = MyApplication.getExpediaAppState();

        hotelName.setText(mHotel.mSummary.mName);
        checkIn.setText(expediaAppState.mArrivalDateParam);
        checkOut.setText(expediaAppState.mDepartureDateParam);
        String guests = expediaAppState.getNumberOfAdults() + " adults";
        if (expediaAppState.getNumberOfChildrenParam() > 0) {
        	guests += ", "+expediaAppState.getNumberOfChildrenParam()+" children";
        }
        numGuests.setText(guests);
        roomType.setText(mRoom.mRoomTypeDescription);
        //bedType.setText(mRoom.bedTypes.get(0).description);

        final NumberFormat currencyFormat = getCurrencyFormat(mRoom.mRateInfo.mChargableRateInfo.mCurrencyCode);
//
//        taxesAndFees.setText(currencyFormat.format(hotelRoom.getTaxesAndFees()));
        taxesAndFees.setText(currencyFormat.format(mRoom.mRateInfo.mChargableRateInfo.mSurchargeTotal));
//
         totalLowPrice.setText(currencyFormat.format(mRoom.mRateInfo.mChargableRateInfo.mNightlyRateTotal));
//
        displayTotalHighPrice();
//        populatePriceBreakdownList(currencyFormat);
	}
	

    private void displayTotalHighPrice() {
        final TextView totalHighPrice = (TextView) mView.findViewById(R.id.highPrice);
        //final ImageView drrIcon = (ImageView) mView.findViewById(R.id.drrPromoImg);
        final TextView drrPromoText = (TextView) mView.findViewById(R.id.drrPromoText);

        if (mRoom.mRateInfo.mChargableRateInfo.mTotalBaseRate == 
        		mRoom.mRateInfo.mChargableRateInfo.mTotalDiscountRate) {
            // if there's no promo, then we make the promo stuff disappear.
            totalHighPrice.setVisibility(TextView.GONE);
            //drrIcon.setVisibility(ImageView.GONE);
            drrPromoText.setVisibility(ImageView.GONE);
        } else {
            // if there is a promo, we make it show up.
            drrPromoText.setText(mRoom.mRateInfo.mPromoDescription);
            totalHighPrice.setVisibility(TextView.VISIBLE);
            //drrIcon.setVisibility(ImageView.VISIBLE);
            drrPromoText.setVisibility(ImageView.VISIBLE);
            final NumberFormat currencyFormat = getCurrencyFormat(mRoom.mRateInfo.mChargableRateInfo.mCurrencyCode);
            totalHighPrice.setText(currencyFormat.format(mRoom.mRateInfo.mChargableRateInfo.mTotalBaseRate));
            totalHighPrice.setPaintFlags(totalHighPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        }
    }
    
    
	public void changeHotelRoom(HotelData hotel, RoomDetails room) {
		Log.i(TAG, "Setting hotelId to "+hotel.mSummary.mHotelId+ "   room: "+room.mRoomTypeDescription);
		if (mHotel == hotel && mRoom == room) {
			return;
		}
		mHotel = hotel;
		mRoom = room;
		
		mCreditCardTypes = new ArrayList<String>(Arrays.asList(new String[] { "VI", "MC", "AE" }));
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
	
	
	/**
	 * (Event hanlder) Handles the complete booking button click. Loads the information from the inputs and
	 * creates a new booking request based on that.
	 */
	private OnClickListener mCompleteBooking = new OnClickListener() {
		@Override
		public void onClick(View v) {
	        final String firstName = ((EditText) mView.findViewById(R.id.guestFirstName)).getText().toString();
	        final String lastName = ((EditText) mView.findViewById(R.id.guestLastName)).getText().toString();
	        final String phone = ((EditText) mView.findViewById(R.id.guestPhoneNumber)).getText().toString();
	        final String email = ((EditText) mView.findViewById(R.id.guestEmail)).getText().toString();

	        final String addressLine1 = ((EditText) mView.findViewById(R.id.billingInformationAddress1)).getText().toString();
	        final String addressLine2 = ((EditText) mView.findViewById(R.id.billingInformationAddress2)).getText().toString();
	        final String city = ((EditText) mView.findViewById(R.id.billingInformationCity)).getText().toString();
	        final String state = ((EditText) mView.findViewById(R.id.billingInformationState)).getText().toString();
	        final String country = ((EditText) mView.findViewById(R.id.billingInformationCountry)).getText().toString();
	        final String zip = ((EditText) mView.findViewById(R.id.billingInformationZip)).getText().toString();

	        final String cardType = ((Spinner) mView.findViewById(R.id.billingInformationCCType)).getSelectedItem().toString();
	        final String cardNumber = ((EditText) mView.findViewById(R.id.billingInformationCCNum)).getText().toString();
	        final String cardExpirationMonth
	            = ((Spinner) mView.findViewById(R.id.billingInformationCCExpMo)).getSelectedItem().toString();
	        final String cardExpirationYear
	            = ((Spinner) mView.findViewById(R.id.billingInformationCCExpYr)).getSelectedItem().toString();
	        final String cardSecurityCode
	            = ((EditText) mView.findViewById(R.id.billingInformationCCSecurityCode)).getText().toString();


	        final int cardExpirationFullYear = Integer.parseInt(cardExpirationYear);
	        final int cardExpirationFullMonth = Integer.parseInt(cardExpirationMonth);

	        final YearMonth expirationDate = new YearMonth(cardExpirationFullYear, cardExpirationFullMonth);

	        final BookingRequest.ReservationInformation reservationInfo = new BookingRequest.ReservationInformation(
	            email, firstName, lastName, phone, null, cardType, cardNumber, cardSecurityCode, expirationDate);

	        final ReservationRoom reservationRoom = new ReservationRoom(
	            reservationInfo.individual.name,
	            SampleApp.selectedRoom,
	            SampleApp.selectedRoom.bedTypes.get(0).id,
	            SampleApp.occupancy());

	        final Address reservationAddress
	            = new Address(Arrays.asList(addressLine1, addressLine2), city, state, country, zip);

	        final BookingRequest request = new BookingRequest(
	            SampleApp.selectedHotel.hotelId,
	            SampleApp.arrivalDate,
	            SampleApp.departureDate,
	            SampleApp.selectedHotel.supplierType,
	            Collections.singletonList(reservationRoom),
	            reservationInfo,
	            reservationAddress);


	        new BookingRequestTask().execute(request);
	        Toast.makeText(getActivity(), "Booking room...", Toast.LENGTH_LONG).show();
	    }
	};
	
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
			if (mHotel.mSummary.mSupplierType.equals("E")) {
				cardType.setSelection(mCreditCardTypes.indexOf("MC"));
				cardNum.setText("5401999999999999");
				// creditCardType: MC (MasterCard)
				// creditCardNumber: 5401999999999999

			} else {
				cardType.setSelection(mCreditCardTypes.indexOf("VI"));
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
