package com.evature.components;

import android.graphics.Color;

public class OutCell extends Cell {

	public OutCell(SimpleDate mDate, float mCellWidth, float mCellHeight, float mPadding) {
		super(mDate, mCellWidth, mCellHeight, mPadding);
		mTextColor = Color.parseColor("#aaaaaa");
	}


}
