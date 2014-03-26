package com.virtual_hotel_agent.search.views.fragments;

import java.util.ArrayList;

import roboguice.fragment.RoboDialogFragment;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.evaapis.crossplatform.EvaTravelers;
import com.virtual_hotel_agent.search.MyApplication;
import com.virtual_hotel_agent.search.R;
import com.virtual_hotel_agent.search.models.expedia.ExpediaAppState;

public class ChildAgeDialogFragment extends RoboDialogFragment {

	public interface ChildAgeDialogListener {
		void onDialogPositiveClick(ChildAgeDialogFragment dialog);
		void onDialogNegativeClick(ChildAgeDialogFragment dialog);
	}
	
	View mView;
	private EditText mNumAdults;
	private EditText mNumChildren;
	private EditText mAgeChild1;
	private EditText mAgeChild2;
	private EditText mAgeChild3;
	private ChildAgeDialogListener mListener;
	
	private EvaTravelers travelers;
	
	public void setTravelers(EvaTravelers travelers) {
		this.travelers = travelers;
	}

	private static int toInt(EditText editText) {
		String text = editText.getText().toString();
		if (text.equals("")) {
			text = "-1";
		}
		int num = -1;
		try {
			num = Integer.parseInt(text);
		}
		catch (NumberFormatException e) {
			num = -1;
		}
		return num;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		if (mNumAdults != null) {
			outState.putInt("adults_per_room", toInt(mNumAdults));
			outState.putInt("children_per_room", toInt(mNumChildren));
			outState.putInt("age_child_1", toInt(mAgeChild1));
			outState.putInt("age_child_2", toInt(mAgeChild2));
			outState.putInt("age_child_3", toInt(mAgeChild3));
		}
		
		super.onSaveInstanceState(outState);
	}
	
	
	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Build the dialog and set up the button click handlers
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
//        // Inflate and set the layout for the dialog
//        // Pass null as the parent view because its going in the dialog layout
        builder.setView(createView(inflater, null, savedInstanceState));
        
        builder.setMessage(R.string.guest_num)
				.setPositiveButton(R.string._ok,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {

								int intNumAdults = toInt(mNumAdults);

								MyApplication.numberOfAdults = intNumAdults;

								int numChildren = toInt(mNumChildren);
								int child1 = toInt(mAgeChild1);
								int child2 = toInt(mAgeChild2);
								int child3 = toInt(mAgeChild3);
								if ((numChildren > 0 && child1 < 0) ||
									(numChildren > 1 && child2 < 0) ||
									(numChildren > 2 && child3 < 0)) {
									
									// values weren't input - same as cancel
									mListener
									.onDialogNegativeClick(ChildAgeDialogFragment.this);
									return;
								}

								MyApplication.childAges = new ArrayList<Integer>();
								if (numChildren > 0) {
									MyApplication.childAges.add(child1);
									if (numChildren > 1) {
										MyApplication.childAges.add(child2);
										if (numChildren > 2) {
											MyApplication.childAges.add(child3);
										}
									}
								}

								// Send the positive button event back to the
								// host activity
								mListener
										.onDialogPositiveClick(ChildAgeDialogFragment.this);
							}
						})
				.setNegativeButton(R.string.cancel,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								// Send the negative button event back to the
								// host activity
								mListener
										.onDialogNegativeClick(ChildAgeDialogFragment.this);
							}
               });
        return builder.create();
    }
	
	public void setListener(ChildAgeDialogListener listener) {
		mListener = listener;
	}
	
	private View createView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		mView = inflater.inflate(R.layout.children_ages, null, false);

		mNumAdults = (EditText)mView.findViewById(R.id.numAdults);
		mNumChildren = (EditText)mView.findViewById(R.id.numChildren);
		mAgeChild1 = (EditText)mView.findViewById(R.id.ageChild1);
		mAgeChild2 = (EditText)mView.findViewById(R.id.ageChild2);
		mAgeChild3 = (EditText)mView.findViewById(R.id.ageChild3);
		
		TextWatcher childrenTextChanged = new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				int num = toInt(mNumChildren);
				mView.findViewById(R.id.linearLayoutChild1).setVisibility( num > 0 ? View.VISIBLE : View.GONE);
				mView.findViewById(R.id.linearLayoutChild2).setVisibility( num > 1 ? View.VISIBLE : View.GONE);
				mView.findViewById(R.id.linearLayoutChild3).setVisibility( num > 2 ? View.VISIBLE : View.GONE);
				if (num > 3) {
					Toast.makeText(ChildAgeDialogFragment.this.getActivity(), "Only 3 or less children per room are allowed", Toast.LENGTH_LONG).show();
				}
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}
			
			@Override
			public void afterTextChanged(Editable s) {
			}
		};
		
		mNumChildren.addTextChangedListener(childrenTextChanged);

		if (savedInstanceState != null) {
			mNumAdults.setText(Integer.toString(savedInstanceState.getInt("adults_per_room", 2)));
			mNumChildren.setText(Integer.toString(savedInstanceState.getInt("children_per_room", 0)));
			mAgeChild1.setText(Integer.toString(savedInstanceState.getInt("age_child_1", 4)));
			mAgeChild2.setText(Integer.toString(savedInstanceState.getInt("age_child_2", 7)));
			mAgeChild3.setText(Integer.toString(savedInstanceState.getInt("age_child_3", 12)));
		}
		else {
			if (travelers != null) {
				mNumAdults.setText(Integer.toString(travelers.allAdults()));
				mNumChildren.setText(Integer.toString(travelers.allChildren()));
			}
			else {
				mNumAdults.setText("2");
				mNumChildren.setText("0");
				mAgeChild1.setText("4");
				mAgeChild2.setText("7");
				mAgeChild3.setText("12");
			}
		}
		
		childrenTextChanged.onTextChanged(null, 0, -1, -2);

		return mView;
	}	
}


