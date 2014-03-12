package com.virtual_hotel_agent.search.views.fragments;

import java.util.Arrays;

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
	
	
	/**
	 * (Event handler) Contains the action to handle the contact choose button.
	 * @param view The view that fired this event.
	 */
	private OnClickListener mChooseContact = new OnClickListener() {
		@Override
		public void onClick(View v) {
	        final Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
	        startActivityForResult(intent, PICK_CONTACT_INTENT);
		}
	};

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
		
		return mView;
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

//        final NumberFormat currencyFormat = getCurrencyFormat(hotel.currencyCode);
//
//        taxesAndFees.setText(currencyFormat.format(hotelRoom.getTaxesAndFees()));
//
//        totalLowPrice.setText(currencyFormat.format(hotelRoom.getTotalRate()));
//
//        displayTotalHighPrice(hotelRoom, hotel.highPrice, currencyFormat);
//        populatePriceBreakdownList(currencyFormat);
	}
	

    private void displayTotalHighPrice() {
        final TextView totalHighPrice = (TextView) mView.findViewById(R.id.highPrice);
        //final ImageView drrIcon = (ImageView) mView.findViewById(R.id.drrPromoImg);
        final TextView drrPromoText = (TextView) mView.findViewById(R.id.drrPromoText);

        if (true) {//hotelRoom.getTotalRate().equals(hotelRoom.getTotalBaseRate())) {
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
            //totalHighPrice.setText(currencyFormat.format(highPrice));
            totalHighPrice.setPaintFlags(totalHighPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        }
    }
    


    
    /**
     * (Event handler) Contains the action to handle the load default billing info button.
     * @param view The view that fired this event.
     */
    public void onLoadDefaultBillingInfoClick(final View view) {
        final EditText addressLine1 = (EditText) mView.findViewById(R.id.billingInformationAddress1);
        final EditText addressLine2 = (EditText) mView.findViewById(R.id.billingInformationAddress2);
        final EditText city = (EditText) mView.findViewById(R.id.billingInformationCity);
        final EditText state = (EditText) mView.findViewById(R.id.billingInformationState);
        final EditText country = (EditText) mView.findViewById(R.id.billingInformationCountry);
        final EditText zip = (EditText) mView.findViewById(R.id.billingInformationZip);

        final Spinner cardType = (Spinner) mView.findViewById(R.id.billingInformationCCType);
        final EditText cardNum = (EditText) mView.findViewById(R.id.billingInformationCCNum);
        final Spinner cardExpirationMonth = (Spinner) mView.findViewById(R.id.billingInformationCCExpMo);
        final Spinner cardExpirationYear = (Spinner) mView.findViewById(R.id.billingInformationCCExpYr);
        final EditText cardSecurityCode = (EditText) mView.findViewById(R.id.billingInformationCCSecurityCode);

        final int yearsInACentury = 100;

        //sorry, but it's just so simple
        addressLine1.setText("travelnow");
        addressLine2.setText("");
        city.setText("Seattle");
        state.setText("WA");
        country.setText("US");
        zip.setText("98004");
        cardType.setSelection(
            Arrays.asList(getResources().getStringArray(R.array.supported_credit_cards)).indexOf("CA"));
        cardNum.setText("5401999999999999");
        cardExpirationMonth.setSelection(Arrays.asList(getResources().getStringArray(R.array.credit_card_months)).indexOf("01"));
        cardExpirationYear.setSelection(Arrays.asList(getResources().getStringArray(R.array.credit_card_years)).indexOf("2015"));// Integer.toString((YearMonth.now().getYear() + 1) % yearsInACentury));
        cardSecurityCode.setText("123");

    }

    /**
     * (Event hanlder) Handles the complete booking button click. Loads the information from the inputs and
     * creates a new booking request based on that.
     * @param view The view that fired the event.
     */
    public void onCompleteBookingButtonClick(final View view) {
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

//        final YearMonth expirationDate = new YearMonth(cardExpirationFullYear, cardExpirationFullMonth);
//
//        final BookingRequest.ReservationInformation reservationInfo = new BookingRequest.ReservationInformation(
//            email, firstName, lastName, phone, null, cardType, cardNumber, cardSecurityCode, expirationDate);
//
//        final ReservationRoom reservationRoom = new ReservationRoom(
//            reservationInfo.individual.name,
//            SampleApp.selectedRoom,
//            SampleApp.selectedRoom.bedTypes.get(0).id,
//            SampleApp.occupancy());
//
//        final Address reservationAddress
//            = new Address(Arrays.asList(addressLine1, addressLine2), city, state, country, zip);
//
//        final BookingRequest request = new BookingRequest(
//            SampleApp.selectedHotel.hotelId,
//            SampleApp.arrivalDate,
//            SampleApp.departureDate,
//            SampleApp.selectedHotel.supplierType,
//            Collections.singletonList(reservationRoom),
//            reservationInfo,
//            reservationAddress);
//
//
//        new BookingRequestTask().execute(request);
        Toast.makeText(getActivity(), "Booking room...", Toast.LENGTH_LONG).show();
    }


	public void changeHotelRoom(HotelData hotel, RoomDetails room) {
		Log.i(TAG, "Setting hotelId to "+hotel.mSummary.mHotelId+ "   room: "+room.mRoomTypeDescription);
		if (mHotel == hotel && mRoom == room) {
			return;
		}
		mHotel = hotel;
		mRoom = room;
		fillData();
	}


}
