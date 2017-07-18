package com.evature.evasdk.user_interface;


import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.style.DynamicDrawableSpan;

import com.evature.evasdk.R;

/***
 * Draw a Red Wavy line below text (actually a horizontal-repeating bitmap)
 * @author iftah
 *
 */
public class ErrorSpan extends DynamicDrawableSpan {
	
	private float width;
    float lineWidth;
    float waveSize;
    int color;


    public ErrorSpan(Resources resources) {
        this(resources, Color.RED, 1, 3);
    }

	public ErrorSpan(Resources resources, int color, int lineWidth, int waveSize) {
		super(DynamicDrawableSpan.ALIGN_BASELINE);
        // Get the screen's density scale
        final float scale = resources.getDisplayMetrics().density;
        // Convert the dps to pixels, based on density scale
        this.lineWidth = lineWidth * scale + 0.5f;
        this.waveSize = waveSize * scale + 0.5f;

        this.color = color;
	}

	@Override
    public Drawable getDrawable() {
		return null;
	}
	
	@Override
    public int getSize(Paint paint, CharSequence text,
                         int start, int end,
                         Paint.FontMetricsInt fm) {
        width = paint.measureText(text, start, end);
	    return (int)width;
	}
                         

	@Override
    public void draw(Canvas canvas, CharSequence text,
                     int start, int end, float x, 
                     int top, int y, int bottom, Paint paint) {
		
        Paint p = new Paint(paint);
        p.setColor(color);
        p.setStrokeWidth(lineWidth);
        p.setAntiAlias(true);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeCap(Paint.Cap.BUTT);
        p.setStrokeJoin(Paint.Join.MITER);
        p.setPathEffect(new CornerPathEffect(2*lineWidth) );   // set the path effect when they join.

        float doubleWaveSize = waveSize * 2;
        Path path = new Path();
        path.moveTo(x, bottom-waveSize);
        for (float i = x; i < x + width; i += doubleWaveSize) {
            path.rLineTo(waveSize, waveSize);
            path.rLineTo(waveSize, -waveSize);
        }
        canvas.save();
        // clip the squigly line, add some pixels above and below to clipping to allow for round path join
        canvas.clipRect(new Rect(
                (int)x, // left
                (int)(bottom-waveSize-2*lineWidth), // top
                (int)(x+width+0.5f), // right
                (int)(bottom+2*lineWidth+0.5)));  // bottom
        canvas.drawPath(path, p);
        canvas.restore();
		canvas.drawText(text.subSequence(start, end).toString(), x, y, paint);
	}
}