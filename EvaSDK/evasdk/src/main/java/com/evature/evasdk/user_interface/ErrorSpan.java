package com.evature.evasdk.user_interface;


import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.style.DynamicDrawableSpan;

import com.evature.evasdk.R;

/***
 * Draw a Red Wavy line below text (actually a horizontal-repeating bitmap)
 * @author iftah
 *
 */
public class ErrorSpan extends DynamicDrawableSpan {
	
	private BitmapDrawable mRedWavy;
	private int mWidth;
	private int mBmpHeight;

	public ErrorSpan(Resources resources) {
		super(DynamicDrawableSpan.ALIGN_BASELINE);
		mRedWavy = new BitmapDrawable(resources, BitmapFactory.decodeResource(resources, R.drawable.evature_error_underline));
		mBmpHeight = mRedWavy.getIntrinsicHeight();
		mRedWavy.setTileModeX(TileMode.REPEAT);
	}

	@Override
    public Drawable getDrawable() {
		return mRedWavy;
	}
	
	@Override
    public int getSize(Paint paint, CharSequence text,
                         int start, int end,
                         Paint.FontMetricsInt fm) {
		mWidth = (int) paint.measureText(text, start, end);
	    return mWidth;
	}
                         

	@Override
    public void draw(Canvas canvas, CharSequence text,
                     int start, int end, float x, 
                     int top, int y, int bottom, Paint paint) {
		
		mRedWavy.setBounds(0, 0, mWidth, mBmpHeight);
		canvas.save();
		canvas.translate(x, bottom-mBmpHeight);
		mRedWavy.draw(canvas);
		canvas.restore();
		canvas.drawText(text.subSequence(start, end).toString(), x, y, paint);
	}
}