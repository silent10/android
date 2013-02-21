package com.evature.search;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import com.evature.components.CalendarView;
import com.evature.components.SimpleDate;

public class CalendarFragment extends Fragment implements EvaDownloaderTaskInterface{

	@Override
	public void onSaveInstanceState(Bundle outState) {		
		outState.putInt(EvaCheckoutActivity.HOTEL_INDEX, mEvaCheckoutActivity.mHotelIndex);		
		super.onSaveInstanceState(outState);
	}

	private static EvaCheckoutActivity mEvaCheckoutActivity = null;
	View mView;
	private CalendarView mCalendar;
	private EditText mNumAdults;	
	static protected EvaRoomsUpdater mRoomsUpdater;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		mView = inflater.inflate(R.layout.calendar,container,false);

		mCalendar = (CalendarView)mView.findViewById(R.id.calendar);
		mNumAdults = (EditText)mView.findViewById(R.id.numAdults);

		ImageButton prev = (ImageButton) mView.findViewById(R.id.btnCalendarPrev);
		prev.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if(mCalendar.prevMonthLegal())
				{
					mCalendar.previousMonth();
				}
			}
		});

		ImageButton next = (ImageButton) mView.findViewById(R.id.btnCalendarNext);
		next.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mCalendar.nextMonth();
			}
		});

		ImageButton ok = (ImageButton) mView.findViewById(R.id.btnCalendarOk);
		ok.setOnClickListener(new OnClickListener() {		

			@Override
			public void onClick(View v) {
				SimpleDate[] selDate = mCalendar.getSelected();
				String dateArrival = "arrivalDate=";
				String dateDeparture = "departureDate=";

				if (selDate != null)
				{
					if (selDate[0] != null)
						dateArrival += selDate[0].month + "/" + selDate[0].day + "/" + selDate[0].year;
					if (selDate[1] != null)
						dateDeparture += selDate[1].month + "/" + selDate[1].day + "/" + selDate[1].year;
				}
				Integer intNumAdults = new Integer(mNumAdults.getText().toString());

				MyApplication.getDb().setArrivalDate(dateArrival);
				MyApplication.getDb().setDepartueDate(dateDeparture);
				MyApplication.getDb().setNumberOfAdults(intNumAdults.intValue());

				mRoomsUpdater = new EvaRoomsUpdater(CalendarFragment.this, mEvaCheckoutActivity.mHotelIndex);

				mRoomsUpdater.execute();

			}
		});

		Calendar rightNow = Calendar.getInstance();
		SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy");
		String from = df.format(rightNow.getTime());
		rightNow.add(Calendar.DAY_OF_MONTH, 1);
		String to = df.format(rightNow.getTime());
		mCalendar.setSelectedDate(from, to);
		mNumAdults.setText("2");

		if (MyApplication.getDb().mArrivalDateParam != null && MyApplication.getDb().mDepartureDateParam != null)
		{
			String arrival = MyApplication.getDb().mArrivalDateParam.replace("arrivalDate=", "");
			String departure = MyApplication.getDb().mDepartureDateParam.replace("departureDate=", "");
			mCalendar.setSelectedDate(arrival, departure);
		}
		if(mRoomsUpdater!=null)
		{
			mRoomsUpdater.attach(this);
			mProgressDialog = ProgressDialog.show(getActivity(),
					"Getting Room Availability", "Contacting search server", true,
					false); 
		}

		return mView;
	}

	public static CalendarFragment newInstance(EvaCheckoutActivity eca) {
		mEvaCheckoutActivity  = eca;
		return new CalendarFragment();
	}

	ProgressDialog mProgressDialog;

	@Override
	public void endProgressDialog(int id) {
		if(mProgressDialog!=null)
		{
			mProgressDialog.dismiss();
			mEvaCheckoutActivity.selectRoom();
		}
		
	}

	@Override
	public void startProgressDialog(int id) {
		if(mRoomsUpdater!=null)
		{
			mProgressDialog = ProgressDialog.show(getActivity(),
					"Getting Room Availability", "Contacting search server", true,
					false);
		}
		
	}

	@Override
	public void endProgressDialogWithError(int id) {
		mRoomsUpdater = null;		

		if(mProgressDialog!=null)
		{
			mProgressDialog.dismiss();
			mProgressDialog = null;
		}
	}

	@Override
	public void updateProgress(int id, int mProgress) {
		// TODO Auto-generated method stub
		
	}

	
}


/*
@Override
public void endProgressDialog() {
	
}

@Override
public void startProgressDialog() {
	
}

@Override
public void endProgressDialogWithError() {

	
}

@Override
public void updateProgress(int mProgress) {
	// TODO Auto-generated method stub

}*/
