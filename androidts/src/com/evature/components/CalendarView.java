package com.evature.components;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class CalendarView extends View {

	private static final float		CELL_RATIO				= 0.65f;
	private static final float		MONTH_RATIO				= 0.6f;

	private static final float		MONTH_PADDING			= 0.2f;
	private static final float		DAYS_PADDING			= 0.2f;
	private static final float		CELL_PADDING			= 0.25f;

	private static final String		DUAL_ATTRIBUTE			= "dual";
	private static final int		EXTRA_MONTH_ROWS		= 4;
	private static final int		ROWS_IN_MONTH			= 6;

	private static final int		BG_COLOR				= Color.parseColor("#FFFFFF");
	private static final int		LINES_COLOR				= Color.parseColor("#000000");
	private static final int		MONTH_COLOR				= Color.parseColor("#000000");
	private static final int		DAYS_COLOR				= Color.parseColor("#FFFFFF");
	private static final int		WEEKEND_COLOR			= Color.parseColor("#d7483e");
	private static final int		DATES_COLOR			= Color.parseColor("#FFFFFF");

	private static final String[]	WEEK_DAYS				= { "Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun" };

	private int						mWidth					= 0;
	private int						mHeight					= 0;
	private float					mCellHeight				= 0;
	private float					mCellWidth				= 0;

	private int						mExtraRows				= 1;
	private int						mCellRows				= 6;
	private int						mCellColls				= 7;

	private ExtraMonthHelper		mMonthHelper;
	private boolean					mIsDualMonth			= false;
	private Cell[][]				mDaysArray				= null;

	private static int				mDayFrom				= -1;
	private static int				mDayTo					= -1;
	private static int				mOldDayFrom				= -1;
	private static int				mOldDayTo				= -1;
	private boolean					mDragFrom				= false;
	private boolean					mDragTo					= false;

	private String					mMonthTitle				= "";

	private OnDateChangeListener	mOnDateChangeListener	= null;

	public CalendarView(Context context, AttributeSet attrs) {
		super(context, attrs);

		String tag = (String) this.getTag();
		mCellRows = ROWS_IN_MONTH + mExtraRows;
		if (tag != null && DUAL_ATTRIBUTE.equals(tag))
		{
			mIsDualMonth = true;
			mCellRows += EXTRA_MONTH_ROWS;
		}
		
		mMonthHelper = new ExtraMonthHelper(mIsDualMonth);
		mMonthHelper.reseHelpersTime();
		
		mMonthTitle = mMonthHelper.getTitle();
	}

	public SimpleDate[] getSelected() {
		SimpleDate[] selectedDates = new SimpleDate[2];
		selectedDates[0] = getDayDate(mDayFrom);
		selectedDates[1] = getDayDate(mDayTo);
		
		selectedDates[0].month++;
		selectedDates[1].month++;
		
		return selectedDates;
	}

	public void setOnDateChangeListener(OnDateChangeListener listener) {
		mOnDateChangeListener = listener;
	}

	private SimpleDate getDayDate(int day) {
		if (day < 0 || day > mDaysArray.length * mDaysArray[0].length)
			return null;

		int i = day / mCellColls;
		int j = day % mCellColls;
		
		return mDaysArray[i][j].mDate;
	}
	
	public boolean prevMonthLegal() {
		return mMonthHelper.isPreviousMonthAllowed();
	}

	public void nextMonth() {
		mMonthHelper.nextMonth();
		
		resetMonthDisplay();
		
		mOldDayFrom = -1;
		mOldDayTo = -1;
		checkDatechange();
	}

	public void previousMonth() {
		mMonthHelper.previousMonth();
		
		resetMonthDisplay();
		
		mOldDayFrom = -1;
		mOldDayTo = -1;
		checkDatechange();
	}
	
	private void resetMonthDisplay() {
		mMonthTitle = mMonthHelper.getTitle();
		initDays();
		setSelectedCells();
		invalidate();
	}

	public void setSelectedDate(String from, String to) {
		SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy");
		Date dateFrom = null;
		Date dateTo = null;
		try
		{
			dateFrom = df.parse(from);
			dateTo = df.parse(to);
		}
		catch (ParseException e)
		{
			e.printStackTrace();
			return;
		}

		Calendar fromCalendar = Calendar.getInstance();
		fromCalendar.setTime(dateFrom);

		Calendar toCalendar = Calendar.getInstance();
		toCalendar.setTime(dateTo);

		// Check if the dates are in the range of two months
		if (!mIsDualMonth)
		{
			if (fromCalendar.get(Calendar.YEAR) != toCalendar.get(Calendar.YEAR) || fromCalendar.get(Calendar.MONTH) != toCalendar
					.get(Calendar.MONTH))
				return;
		}
		else
		{
			Calendar fromNext = Calendar.getInstance();
			fromNext.setTime(dateFrom);
			fromNext.add(Calendar.MONTH, 1);

			if ((toCalendar.get(Calendar.MONTH) != fromCalendar.get(Calendar.MONTH) || toCalendar.get(Calendar.YEAR) != fromCalendar
					.get(Calendar.YEAR)) && (toCalendar.get(Calendar.MONTH) != fromNext.get(Calendar.MONTH) || toCalendar
					.get(Calendar.YEAR) != fromNext.get(Calendar.YEAR)))
				return;
		}
		
		mMonthHelper.reseHelpersTime(fromCalendar);
		initDays();

		SimpleDate simpleFrom = new SimpleDate(fromCalendar.get(Calendar.YEAR), fromCalendar.get(Calendar.MONTH),
				fromCalendar.get(Calendar.DAY_OF_MONTH));

		SimpleDate simpleTo = new SimpleDate(toCalendar.get(Calendar.YEAR), toCalendar.get(Calendar.MONTH),
				toCalendar.get(Calendar.DAY_OF_MONTH));
		
		for (int i = 0; i < mDaysArray.length; i++)
		{
			for (int j = 0; j < mDaysArray[i].length; j++)
			{
				if (mDaysArray[i][j].mDate.equals(simpleFrom))
					mDayFrom = i * mCellColls + j;
				if (mDaysArray[i][j].mDate.equals(simpleTo))
					mDayTo = i * mCellColls + j;
			}
		}

		resetMonthDisplay();
		checkDatechange();
	}
	
	private void initDays() {
		int arrayRows = mCellRows - mExtraRows;
		int arrayCols = mCellColls;
		
		mDaysArray = new Cell[arrayRows][arrayCols];
		mMonthHelper.resetIterators();
		
		int row = 0;
		int col = 0;
		
		do
		{
			SimpleDate day = mMonthHelper.getDay();
			if (mMonthHelper.isBeforeToday(day)==false)
			{
				if (mMonthHelper.isToday())
					mDaysArray[row][col] = new TodayCell(day, mCellWidth, mCellHeight, CELL_PADDING);
				else
				{
					
				}
					mDaysArray[row][col] = new MonthCell(day, mCellWidth, mCellHeight, CELL_PADDING);
			}
			else
			{
				mDaysArray[row][col] = new OutCell(day, mCellWidth, mCellHeight, CELL_PADDING);
			}
			col++;
			if (col >= arrayCols)
			{
				col = 0;
				row++;
			}
		}
		while(mMonthHelper.goToNextDay());
		
		
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {

		int i = (int) (event.getY() / mCellHeight);
		int j = (int) (event.getX() / mCellWidth);

		// Touch on non relevant area
		if (!(i > 0 && i < mCellRows) || !(j >= 0 && j < mCellColls) || (mDaysArray[i - 1][j] instanceof OutCell))
			return false;

		if (event.getAction() == MotionEvent.ACTION_DOWN)
		{
			// Cell is not selected and the next cell is in this month
			if (!mDaysArray[i - 1][j].isSelected() && !(mDaysArray[i - 1 + ((j + 1) / mCellColls)][(j + 1) % mCellColls] instanceof OutCell))
			{
				mDayTo = (mDayFrom = ((i - 1) * mCellColls + j)) + 1;
			}

			// Cell is selected
			if (mDaysArray[i - 1][j].isSelected())
			{
				mDragFrom = false;
				mDragTo = false;

				// Cell is From or To
				int tmpDay = (i - 1) * mCellColls + j;
				if (tmpDay == mDayFrom)
					mDragFrom = true;
				else if (tmpDay == mDayTo)
					mDragTo = true;
				// Cell is in the selection range and the next cell is in this month
				else if (!(mDaysArray[i - 1 + ((j + 1) / mCellColls)][(j + 1) % mCellColls] instanceof OutCell))
					mDayTo = (mDayFrom = ((i - 1) * mCellColls + j)) + 1;
				checkDatechange();
				return true;
			}
		}
		if (event.getAction() == MotionEvent.ACTION_MOVE)
		{
			if (mDragFrom)
			{
				mDayFrom = ((i - 1) * mCellColls + j);
			}
			if (mDragTo)
			{
				mDayTo = ((i - 1) * mCellColls + j);
			}
		}
		setSelectedCells();
		invalidate();
		checkDatechange();
		return super.onTouchEvent(event);
	}

	private void setSelectedCells() {
		if (mDayFrom < 0 || mDayTo < 0)
			return;

		if (mDayFrom > mDayTo)
		{
			mDayFrom += mDayTo;
			mDayTo = mDayFrom - mDayTo;
			mDayFrom -= mDayTo;
			if (mDragFrom || mDragTo)
			{
				mDragFrom = !mDragFrom;
				mDragTo = !mDragTo;
			}
		}

		int totalDays = mDaysArray.length * mDaysArray[0].length;
		for (int day = 0; day < totalDays; day++)
		{
			int i = day / mCellColls;
			int j = day % mCellColls;
			if (day > mDayFrom && day < mDayTo)
			{
				mDaysArray[i][j].setSelected(true);
				mDaysArray[i][j].setFromTo(false);
			}
			else if (day == mDayFrom || day == mDayTo)
			{
				mDaysArray[i][j].setFromTo(true);
				mDaysArray[i][j].setSelected(true);
			}
			else
			{
				mDaysArray[i][j].setFromTo(false);
				mDaysArray[i][j].setSelected(false);
			}
		}
	}

	private void checkDatechange() {
		if (mOldDayFrom != mDayFrom || mOldDayTo != mDayTo)
		{
			mOldDayFrom = mDayFrom;
			mOldDayTo = mDayTo;
			if (mOnDateChangeListener != null)
				mOnDateChangeListener.dateChanged(getSelected());
		}
	}
	
	/**********************************
	 *        Measures
	 * ********************************/

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		// Get the width that was set by the parent
		float parentWidth = MeasureSpec.getSize(widthMeasureSpec);
		float parentHeight = MeasureSpec.getSize(heightMeasureSpec);
		
		// New array for holding the possible sizes of cells and calendar area
		// Easy to fix for more than 2 months
		float[][] sizesArray = new float[2][5];
		mCellRows = ROWS_IN_MONTH;
		mCellColls = 7;
		int numOfIterations = 1;
		if (mIsDualMonth)
		{
			mCellRows += EXTRA_MONTH_ROWS;
			sizesArray = new float[4][5];
			numOfIterations = 2;
		}
		
		// Calculating proportions for vertical option
		for (int i = 0, month = 1; i < numOfIterations * 2; i += 2, month++)
		{
			float totalRows = mCellRows / month + mExtraRows;
			float totalCols = mCellColls * month;

			// Calculation by maximum width
			sizesArray[i][0] = totalRows * parentWidth / totalCols;
			sizesArray[i][1] = parentWidth;
			sizesArray[i][2] = (sizesArray[i][0] <= parentHeight) ? parentWidth / totalCols : -1;
			sizesArray[i][3] = totalCols;
			sizesArray[i][4] = totalRows;
			// Calculation by maximum height
			sizesArray[i + 1][0] = parentHeight;
			sizesArray[i + 1][1] = totalCols * parentHeight / totalRows;
			sizesArray[i + 1][2] = (sizesArray[i + 1][1] <= parentWidth) ? parentHeight / totalRows : -1;
			sizesArray[i + 1][3] = totalCols;
			sizesArray[i + 1][4] = totalRows;
		}
		
		float max = 0;
		for (int i=0; i<sizesArray.length; i++)
		{
			if (sizesArray[i][2] > max)
			{
				mHeight = (int)sizesArray[i][0];
				mWidth = (int)sizesArray[i][1];
				max = sizesArray[i][2];
				mCellColls = (int)sizesArray[i][3];
				mCellRows = (int)sizesArray[i][4];
			}
		}

		setMeasuredDimension(mWidth, mHeight);
		updateCellSize();
		initDays();
		setSelectedCells();
	}

	private void updateCellSize() {
		mCellHeight = (float) mHeight / mCellRows;
		mCellWidth = (float) mWidth / mCellColls;
	}
	
	/**********************************
	 *        Drawing functions
	 * ********************************/
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		Paint paint = new Paint();

		// Draw background
		paint.setColor(BG_COLOR);
		canvas.drawRect(0, 0, mWidth, mHeight, paint);

		// Draw cells// TODO Auto-generated method stub
		drawCells(canvas);

		// Draw horizontal lines
		paint.setColor(LINES_COLOR);
		for (int i = 0; i <= mCellRows; i++)
		{
			float yStart = (i == mCellRows) ? mHeight - 1 : mCellHeight * i;
			canvas.drawLine(0, yStart, mWidth, yStart, paint);
		}

		// Draw vertical lines
		for (int i = 1; i < mCellRows; i++)
		{
			for (int j = 0; j <= mCellColls; j++)
			{
				float xStart = (j == mCellColls) ? mWidth - 1 : (j * mCellWidth);
				float yStart = i * mCellHeight + (mCellHeight * ((1 - CELL_RATIO) / 2));

				canvas.drawLine(xStart, yStart, xStart, yStart + mCellHeight * CELL_RATIO, paint);
			}
		}

		// Draw month title
		drawMonthTitle(canvas);
		
		// Draw days titles
		drawDaysTitles(canvas);
	}
	
	private void drawMonthTitle(Canvas canvas) {
		Paint paint = new Paint();

		paint.setColor(MONTH_COLOR);
		float monthCellHeight = mCellHeight * MONTH_RATIO;
		canvas.drawRect(0, 0, mWidth, monthCellHeight, paint);

		paint.setColor(DATES_COLOR);
		paint.setAntiAlias(true);
		paint.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));

		float textHeight = monthCellHeight * (1 - MONTH_PADDING * 2);
		paint.setTextSize(pickTextSize(textHeight));
		canvas.drawText(mMonthTitle, monthCellHeight * MONTH_PADDING * 2,
				monthCellHeight * MONTH_PADDING / 2 + textHeight, paint);
	}
	
	private void drawDaysTitles(Canvas canvas) {
		Paint paint = new Paint();
		paint.setColor(MONTH_COLOR);
		float daysOffset = mCellHeight * MONTH_RATIO;
		float daysCellHeight = mCellHeight - daysOffset;
		canvas.drawRect(0, daysOffset, mWidth, mCellHeight, paint);

		paint.setColor(DAYS_COLOR);
		paint.setAntiAlias(true);
		paint.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));

		float textHeight = daysCellHeight * (1 - DAYS_PADDING * 2);
		paint.setTextSize(pickTextSize(textHeight));

		for (int i = 0; i < WEEK_DAYS.length; i++)
		{
			if (i >= WEEK_DAYS.length - 2)
				paint.setColor(WEEKEND_COLOR);
			Rect bounds = new Rect();
			paint.getTextBounds(WEEK_DAYS[i], 0, WEEK_DAYS[i].length(), bounds);
			float xOffset = (mCellWidth - bounds.right) / 2 + i * mCellWidth;
			float yOffset = daysOffset + daysCellHeight * MONTH_PADDING / 2 + textHeight;
			canvas.drawText(WEEK_DAYS[i], xOffset, yOffset, paint);
		}
		
		if (mCellColls > WEEK_DAYS.length)
		{
			paint.setColor(DAYS_COLOR);
			for (int i = 0; i < WEEK_DAYS.length; i++)
			{
				if (i >= WEEK_DAYS.length - 2)
					paint.setColor(WEEKEND_COLOR);
				Rect bounds = new Rect();
				paint.getTextBounds(WEEK_DAYS[i], 0, WEEK_DAYS[i].length(), bounds);
				float xOffset = (mCellWidth - bounds.right) / 2 + (i + WEEK_DAYS.length) * mCellWidth;
				float yOffset = daysOffset + daysCellHeight * MONTH_PADDING / 2 + textHeight;
				canvas.drawText(WEEK_DAYS[i], xOffset, yOffset, paint);
			}
		}
	}
	
	private void drawCells(Canvas canvas) {
		float textHeight = mCellHeight * (1 - CELL_PADDING * 2);
		float textSize = pickTextSize(textHeight);

		for (int i = 0; i < mDaysArray.length; i++)
		{
			for (int j = 0; j < mDaysArray[i].length; j++)
			{
				if (mDaysArray[i][j] != null)
					mDaysArray[i][j].draw(canvas, j * mCellWidth, (i + 1) * mCellHeight, textSize);
			}
		}
	}

	private float pickTextSize(float size) {
		Paint paint = new Paint();
		paint.setTextSize(100);

		Rect bounds = new Rect();
		paint.getTextBounds("J", 0, 1, bounds);

		float textHeight = bounds.bottom - bounds.top;

		return ((size * 100) / textHeight);
	}

	
}
