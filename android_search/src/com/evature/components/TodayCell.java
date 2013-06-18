package com.evature.components;

import android.graphics.Color;

public class TodayCell extends Cell {

	public TodayCell(SimpleDate mDate, float mCellWidth, float mCellHeight, float mPadding) {
		super(mDate, mCellWidth, mCellHeight, mPadding);
		mBgColor=Color.parseColor("#000000");//("#cfd9f3");
		mTextColor=Color.parseColor("#000000");
	}

}
