package com.evature.components;

import android.graphics.Color;

public class MonthCell extends Cell {

	public MonthCell(SimpleDate mDate, float mCellWidth, float mCellHeight, float mPadding) {
		super(mDate, mCellWidth, mCellHeight, mPadding);
		mTextColor = Color.parseColor("#000000");
	}

}
