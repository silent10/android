package com.virtual_hotel_agent.search.util;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Log;

import com.espian.showcaseview.drawing.ClingDrawer;
import com.virtual_hotel_agent.search.R;

/**
 * Iftah: Attempt at rectangular Cling
 */
public class RectClingDrawerImpl implements ClingDrawer {

    private Paint mEraser;
    private Drawable mShowcaseDrawable;
    private Rect mShowcaseRect;
    int width = 800;
    int height = 300;
    int radius = 20;

    public RectClingDrawerImpl(Resources resources, int showcaseColor) {
        PorterDuffXfermode mBlender = new PorterDuffXfermode(PorterDuff.Mode.CLEAR);
        mEraser = new Paint();
        mEraser.setColor(0xFFFFFF);
        mEraser.setAlpha(0);
        mEraser.setXfermode(mBlender);
        mEraser.setAntiAlias(true);

        mShowcaseDrawable = resources.getDrawable(R.drawable.rect_cling_bleached );
        mShowcaseDrawable.setColorFilter(showcaseColor, PorterDuff.Mode.MULTIPLY);
    }

    @Override
    public void drawShowcase(Canvas canvas, float x, float y, float scaleMultiplier, float radius) {
        Matrix mm = new Matrix();
        mm.postScale(scaleMultiplier, scaleMultiplier, x, y);
        canvas.setMatrix(mm);

        RectF rect = new RectF(x - width/2, y - height/2, x + width/2, y+height/2);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
        	canvas.drawRoundRect(rect, radius, radius, mEraser);
        }

//        mShowcaseDrawable.setBounds(mShowcaseRect);
//        mShowcaseDrawable.draw(canvas);

        canvas.setMatrix(new Matrix());
    }

    @Override
    public int getShowcaseWidth() {
        return width;//mShowcaseDrawable.getIntrinsicWidth();
    }

    @Override
    public int getShowcaseHeight() {
        return height; //mShowcaseDrawable.getIntrinsicHeight();
    }

    /**
     * Creates a {@link android.graphics.Rect} which represents the area the showcase covers. Used
     * to calculate where best to place the text
     *
     * @return true if voidedArea has changed, false otherwise.
     */
    public boolean calculateShowcaseRect(float x, float y, float scaleMultiplier) {

        if (mShowcaseRect == null) {
            mShowcaseRect = new Rect();
        }

        int cx = (int) x, cy = (int) y;
        int dw = (int)(getShowcaseWidth()*scaleMultiplier);
        int dh = (int)(getShowcaseHeight()*scaleMultiplier);

        if (mShowcaseRect.left == cx - dw / 2) {
            return false;
        }

        Log.d("ShowcaseView", "Recalculated:  cx: "+cx+ " cy: "+cy+"  dw: "+dw+"  dh: "+dh);

        mShowcaseRect.left = cx - dw / 2;
        mShowcaseRect.top = cy - dh / 2;
        mShowcaseRect.right = cx + dw / 2;
        mShowcaseRect.bottom = cy + dh / 2;

        return true;

    }

    @Override
    public Rect getShowcaseRect() {
        return mShowcaseRect;
    }

	@Override
	public Path getPath(int showcaseX, int showcaseY) {
		RectF rect = new RectF(mShowcaseRect); //showcaseX - width/2, showcaseY - height/2, showcaseX + width/2, showcaseY+height/2);
		Path path = new Path();
		path.addRoundRect(rect, radius, radius, Direction.CW);
		return path;
	}

	public void setWidth(int w) {
		width = w;
	}

	public void setHeight(int h) {
		height = h;
	}

}
