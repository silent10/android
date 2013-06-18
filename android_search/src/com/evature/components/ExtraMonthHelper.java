package com.evature.components;

import java.text.DateFormatSymbols;
import java.util.Calendar;

import android.util.Log;
import android.util.MonthDisplayHelper;

public class ExtraMonthHelper {

	private MonthDisplayHelper[]	mMonthHelpers			= null;
	
	private Calendar				mRightNow				= null;
	
	private boolean 				mIsDual 				= false;
	
	private int						mRow					= 0;
	private int						mColumn					= 0;
	private int						mCurrentHelper			= 0;
	private int						mStopRow				= 0;
	
	private static final int 		HELPER_MAX_ROW 			= 5;
	private static final int 		HELPER_MAX_COL 			= 6;

	private static final String TAG = "ExtraMonthHelper";
	
	public ExtraMonthHelper(boolean isDual) {
		mIsDual = isDual;
	}
	
	public void reseHelpersTime() {
		mRightNow = Calendar.getInstance();
		reseHelpersTime(mRightNow);
	}
	
	public void reseHelpersTime(Calendar startDate) {
		Log.i(TAG, "Start date, year="+startDate.get(Calendar.YEAR));
		Log.i(TAG, "Start date, month="+startDate.get(Calendar.MONTH));
		Log.i(TAG, "Start date, day="+startDate.get(Calendar.DAY_OF_MONTH));
		
		if (mIsDual)
		{
			mMonthHelpers = new MonthDisplayHelper[2];
			mMonthHelpers[0] = new MonthDisplayHelper(startDate.get(Calendar.YEAR), startDate.get(Calendar.MONTH));
			Calendar rightAfter = (Calendar)startDate.clone();
			rightAfter.add(Calendar.MONTH, 1);
			mMonthHelpers[1] = new MonthDisplayHelper(rightAfter.get(Calendar.YEAR), rightAfter.get(Calendar.MONTH));
		
		}
		else
		{
			mMonthHelpers = new MonthDisplayHelper[1];
			mMonthHelpers[0] = new MonthDisplayHelper(startDate.get(Calendar.YEAR), startDate.get(Calendar.MONTH));
		}
	}
	
	public String getTitle() {
		DateFormatSymbols df = new DateFormatSymbols();
		String title = df.getMonths()[mMonthHelpers[0].getMonth()] + ", " + mMonthHelpers[0].getYear();

		if (mIsDual)
		{


			if (mMonthHelpers[1].getYear() != mMonthHelpers[0].getYear())
			{
				title = df.getMonths()[mMonthHelpers[0].getMonth()] + ", " + mMonthHelpers[0].getYear() + " - ";
				title += df.getMonths()[mMonthHelpers[1].getMonth()] + ", " + mMonthHelpers[1].getYear();
			}
			else
			{
				title = df.getMonths()[mMonthHelpers[0].getMonth()] + " - " + df.getMonths()[mMonthHelpers[1]
						.getMonth()];
				title += ", " + mMonthHelpers[0].getYear();
			}
		}
		
		return title;
	}
	
	public void resetIterators() {
		mRow = 0;
		mColumn = 0;
		mCurrentHelper = 0;
		mStopRow = HELPER_MAX_ROW;
	}
	
	public boolean isWithinMonth() {
		return mMonthHelpers[mCurrentHelper].isWithinCurrentMonth(mRow, mColumn);
	}
	
	public boolean isBeforeToday(SimpleDate day)
	{
		if(day.year < mRightNow.get(Calendar.YEAR))
		{
			return true;
		}
		
		if(day.year > mRightNow.get(Calendar.YEAR))
		{
			return false;
		}
		
		if(day.month < mRightNow.get(Calendar.MONTH))
		{
			return true;
		}
		
		if(day.month > mRightNow.get(Calendar.MONTH))
		{
			return false;
		}
		
		if(day.day < mRightNow.get(Calendar.DAY_OF_MONTH))
		{
			return true;
		}
		
		return false;
	}
	
	
	public boolean isToday() {
		boolean year = mMonthHelpers[mCurrentHelper].getYear() == mRightNow.get(Calendar.YEAR);
		boolean month = mMonthHelpers[mCurrentHelper].getMonth() == mRightNow.get(Calendar.MONTH);
		boolean day = mMonthHelpers[mCurrentHelper].getDayAt(mRow, mColumn) == mRightNow.get(Calendar.DAY_OF_MONTH);
		
		return (year && month && day);
	}
	
	boolean isPreviousMonthAllowed()
	{
		int currentMonth = mMonthHelpers[0].getMonth();
		
		return (currentMonth > mRightNow.get(Calendar.MONTH));
		
	}
	
	public SimpleDate getDay() {
		int day = mMonthHelpers[mCurrentHelper].getDayAt(mRow, mColumn);
		int currentYear = mMonthHelpers[mCurrentHelper].getYear();
		int currentMonth = mMonthHelpers[mCurrentHelper].getMonth();
		if (!isWithinMonth())
		{
			MonthDisplayHelper newMonth = new MonthDisplayHelper(currentYear, currentMonth);
			if (mCurrentHelper == 0)
				newMonth.previousMonth();
			else
				newMonth.nextMonth();
			return new SimpleDate(newMonth.getYear(), newMonth.getMonth(), day);				
		}
		else
		{		
			return new SimpleDate(currentYear, currentMonth, day);	 
		}
	}
	
	public void nextMonth() {
		for (MonthDisplayHelper mdh : mMonthHelpers)
			mdh.nextMonth();
	}
	
	public void previousMonth() {
		for (MonthDisplayHelper mdh : mMonthHelpers)
			mdh.previousMonth();
	}
	
	public boolean goToNextDay() {
		boolean moreDaysLeft = true;
		
		// Next column
		mColumn++;
		if (mColumn > HELPER_MAX_COL)
		{
			mColumn = 0;
			mRow++;
		}
		
		// If row should be on next month
		if (mIsDual)
		{
			if (mRow > 1 && mCurrentHelper == 0 && !mMonthHelpers[0].isWithinCurrentMonth(mRow, mColumn))
			{
				mStopRow = (HELPER_MAX_ROW - mRow >= 1) ? HELPER_MAX_ROW : HELPER_MAX_ROW - 1;
				mRow = 0;
				mCurrentHelper++;
			}
		}
		if (mRow > mStopRow)
		{
			moreDaysLeft = false;
		}
		
		return moreDaysLeft;
	}
	
}
