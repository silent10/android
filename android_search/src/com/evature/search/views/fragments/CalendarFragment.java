package com.evature.search.views.fragments;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import roboguice.fragment.RoboFragment;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import com.evature.components.CalendarView;
import com.evature.components.SimpleDate;
import com.evature.search.MyApplication;
import com.evature.search.R;
import com.evature.search.controllers.activities.EvaCheckoutActivity;
import com.evature.search.controllers.web_services.EvaDownloaderTaskInterface;
import com.evature.search.controllers.web_services.EvaRoomsUpdaterTask;
import com.evature.search.models.expedia.EvaXpediaDatabase;
import com.evature.search.models.expedia.ExpediaRequestParameters;

public class CalendarFragment extends RoboFragment implements EvaDownloaderTaskInterface{

	@Override
	public void onSaveInstanceState(Bundle outState) {		
		outState.putInt(EvaCheckoutActivity.HOTEL_INDEX, mEvaCheckoutActivity.getHotelIndex());		
		super.onSaveInstanceState(outState);
	}

	private static EvaCheckoutActivity mEvaCheckoutActivity = null;
	View mView;
	private CalendarView mCalendar;
	private EditText mNumAdults;	
	static protected EvaRoomsUpdaterTask mRoomsUpdater;
	
	static final SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy");


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
					if (selDate[0] != null && selDate[1] != null && selDate[1].month <= selDate[0].month && selDate[1].day <= selDate[0].day  && selDate[1].year <= selDate[0].year) {
						// selDate[1] must be at least one day after selDate[0]
						Calendar calendar = Calendar.getInstance();
						calendar.set(selDate[0].year, selDate[0].month, selDate[0].day);
						calendar.add(Calendar.DAY_OF_MONTH, 1);
						selDate[1] = new SimpleDate( calendar.get(Calendar.YEAR), 
													 calendar.get(Calendar.MONTH),
													 calendar.get(Calendar.DAY_OF_MONTH));
						
					}
					if (selDate[0] != null)
						dateArrival += selDate[0].month + "/" + selDate[0].day + "/" + selDate[0].year;
					if (selDate[1] != null)
						dateDeparture += selDate[1].month + "/" + selDate[1].day + "/" + selDate[1].year;
				}
				int intNumAdults = Integer.parseInt(mNumAdults.getText().toString());

				ExpediaRequestParameters db = MyApplication.getExpediaRequestParams();
				db.setArrivalDate(dateArrival);
				db.setDepartueDate(dateDeparture);
				db.setNumberOfAdults(intNumAdults);

				mRoomsUpdater = new EvaRoomsUpdaterTask(CalendarFragment.this.getActivity(),
						mEvaCheckoutActivity.getHotelIndex());
				mRoomsUpdater.execute();

			}
		});

		Calendar rightNow = Calendar.getInstance();
		String from = df.format(rightNow.getTime());
		rightNow.add(Calendar.DAY_OF_MONTH, 1);
		String to = df.format(rightNow.getTime());
		mCalendar.setSelectedDate(from, to);
		mNumAdults.setText("2");

		ExpediaRequestParameters db = MyApplication.getExpediaRequestParams();
		if (db.mArrivalDateParam != null && db.mDepartureDateParam != null)
		{
			String arrival = db.mArrivalDateParam.replace("arrivalDate=", "");
			String departure = db.mDepartureDateParam.replace("departureDate=", "");
			mCalendar.setSelectedDate(arrival, departure);
		}
		if(mRoomsUpdater!=null)
		{
			mRoomsUpdater.attach(this);
		}

		return mView;
	}

	public static CalendarFragment newInstance(EvaCheckoutActivity eca) {
		mEvaCheckoutActivity  = eca;
		return new CalendarFragment();
	}

	ProgressDialog mProgressDialog;

	@Override
	public void endProgressDialog(int id, String result) {
		if(mProgressDialog!=null)
		{
			mProgressDialog.dismiss();
			mEvaCheckoutActivity.selectRoom();
		}
		mRoomsUpdater = null;
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
	public void endProgressDialogWithError(int id, String result) {
		mRoomsUpdater = null;		

		if(mProgressDialog!=null)
		{
			mProgressDialog.dismiss();
			mProgressDialog = null;
		}
	}

	@Override
	public void updateProgress(int id, DownloaderStatus mProgress) {
	}

	
}


