package com.evature.search.views.fragments;

import roboguice.fragment.RoboFragment;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.evature.search.MyApplication;
import com.evature.search.R;
import com.evature.search.controllers.activities.EvaCheckoutActivity;
import com.evature.search.controllers.web_services.EvaDownloaderTaskInterface;
import com.evature.search.controllers.web_services.EvaRoomsUpdaterTask;
import com.evature.search.models.expedia.EvaXpediaDatabase;

public class ChildAgeFragment extends RoboFragment implements EvaDownloaderTaskInterface{

	@Override
	public void onSaveInstanceState(Bundle outState) {		
		outState.putInt(EvaCheckoutActivity.HOTEL_INDEX, mEvaCheckoutActivity.getHotelIndex());		
		super.onSaveInstanceState(outState);
	}

	private static EvaCheckoutActivity mEvaCheckoutActivity = null;
	View mView;
	private EditText mNumAdults;
	private EditText mNumChildren;
	private EditText mAgeChild1;
	private EditText mAgeChild2;
	private EditText mAgeChild3;
	static protected EvaRoomsUpdaterTask mRoomsUpdater;
	
	private static int toInt(EditText editText) {
		String text = editText.getText().toString();
		if (text.equals("")) {
			text = "0";
		}
		int num = 0;
		try {
			num = Integer.parseInt(text);
		}
		catch (NumberFormatException e) {
			num = 0;
		}
		return num;
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		mView = inflater.inflate(R.layout.children_ages, container, false);

		mNumAdults = (EditText)mView.findViewById(R.id.numAdults);
		mNumChildren = (EditText)mView.findViewById(R.id.numChildren);
		mAgeChild1 = (EditText)mView.findViewById(R.id.ageChild1);
		mAgeChild2 = (EditText)mView.findViewById(R.id.ageChild2);
		mAgeChild3 = (EditText)mView.findViewById(R.id.ageChild3);
		
		mNumChildren.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				int num = toInt(mNumChildren);
				if (num > 0) {
					mView.findViewById(R.id.linearLayoutChild1).setVisibility(View.VISIBLE);
					if (num > 1) {
						mView.findViewById(R.id.linearLayoutChild2).setVisibility(View.VISIBLE);
						if (num > 2) {
							mView.findViewById(R.id.linearLayoutChild3).setVisibility(View.VISIBLE);
							if (num > 3) {
								Toast.makeText(ChildAgeFragment.this.getActivity(), "Only 3 or less children per room are allowed", Toast.LENGTH_LONG).show();
							}
						}
						else {
							mView.findViewById(R.id.linearLayoutChild3).setVisibility(View.GONE);
						}
					}
					else {
						mView.findViewById(R.id.linearLayoutChild2).setVisibility(View.GONE);
						mView.findViewById(R.id.linearLayoutChild3).setVisibility(View.GONE);
					}
				}
				else {
					mView.findViewById(R.id.linearLayoutChild1).setVisibility(View.GONE);
					mView.findViewById(R.id.linearLayoutChild2).setVisibility(View.GONE);
					mView.findViewById(R.id.linearLayoutChild3).setVisibility(View.GONE);
				}
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}
			
			@Override
			public void afterTextChanged(Editable s) {
			}
		});

		ImageButton ok = (ImageButton) mView.findViewById(R.id.btnAgesOk);
		ok.setOnClickListener(new OnClickListener() {		

			@Override
			public void onClick(View v) {
				
				int intNumAdults = toInt(mNumAdults);

				EvaXpediaDatabase db = MyApplication.getDb();
				db.setNumberOfAdults(intNumAdults);
				
				db.setNumberOfChildrenParam(toInt(mNumChildren));
				db.setAgeChild1(toInt(mAgeChild1));
				db.setAgeChild2(toInt(mAgeChild2));
				db.setAgeChild3(toInt(mAgeChild3));

				mRoomsUpdater = new EvaRoomsUpdaterTask(ChildAgeFragment.this, ChildAgeFragment.this.getActivity(),
						mEvaCheckoutActivity.getHotelIndex());
				mRoomsUpdater.execute();

			}
		});

		mNumChildren.setText("0");
		mNumAdults.setText("2");
		mAgeChild1.setText("4");
		mAgeChild2.setText("7");
		mAgeChild3.setText("10");
		

		if(mRoomsUpdater!=null)
		{
			mRoomsUpdater.attach(this);
		}

		return mView;
	}

	public static ChildAgeFragment newInstance(EvaCheckoutActivity eca) {
		mEvaCheckoutActivity  = eca;
		return new ChildAgeFragment();
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


