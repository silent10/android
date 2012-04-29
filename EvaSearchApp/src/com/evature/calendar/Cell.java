package com.evature.calendar;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

public abstract class Cell {

	public SimpleDate	mDate;
	protected int		mTextColor;
	protected int		mBgColor;
	protected int		mSelectedColor;
	protected int		mFromToColor;
	protected float		mCellWidth;
	protected float		mCellHeight;
	protected float		mPadding;

	protected boolean	mIsSelected	= false;
	protected boolean	mIsFromTo	= false;

	public Cell(SimpleDate mDate, float mCellWidth, float mCellHeight, float mPadding) {
		super();
		this.mDate = mDate;
		this.mCellWidth = mCellWidth;
		this.mCellHeight = mCellHeight;
		this.mPadding = mPadding;
		this.mSelectedColor = Color.parseColor("#fbe060");
		this.mFromToColor = Color.parseColor("#fbca33");
	}

	public void setSelected(boolean selected) {
		mIsSelected = selected;
	}
	
	public boolean isSelected() {
		return mIsSelected;
	}
	
	public void setFromTo(boolean fromto) {
		mIsFromTo = fromto;
	}
	
	public boolean isFromTo() {
		return mIsFromTo;
	}

	void draw(Canvas canvas, float x, float y, float textSize) {
		Paint paint = new Paint();
		paint.setAntiAlias(true);
		if (mIsSelected)
		{
			paint.setColor(mSelectedColor);
			if (mIsFromTo)
				paint.setColor(mFromToColor);
			canvas.drawRect(x, y, x + mCellWidth, y + mCellHeight, paint);
		}
		paint.setColor(mTextColor);
		

		paint.setTextSize(textSize);
		
		String strDay = Integer.toString(mDate.day);
		Rect bounds = new Rect();
		paint.getTextBounds(strDay, 0, strDay.length(), bounds);
		
		float xOffset = x + (mCellWidth - (float)bounds.right)/2;
		float yOffset = y + mCellHeight * mPadding + (bounds.bottom - bounds.top);
		
		canvas.drawText(strDay, xOffset, yOffset, paint);
		
	}

}
