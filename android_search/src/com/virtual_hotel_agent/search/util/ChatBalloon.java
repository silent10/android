package com.virtual_hotel_agent.search.util;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;

import com.evature.util.Log;

// based on http://developer.android.com/reference/android/view/ViewGroup.html
// https://developer.android.com/training/material/shadows-clipping.html

public class ChatBalloon extends ViewGroup {

	private static final String TAG = "ChatBalloon";
	private int mColor;
	private static int POINTER_WIDTH = 20; // in Device independant pixels
	private static int POINTER_HEIGHT = 15; // in Device independant pixels
	private int mPointerWidth;
	private int mPointerHeight;
	private Paint mBalloonPaint;
	private Path mPointerPath;
	private Rect mRect = new Rect();
	

	public ChatBalloon(Context context) {
        super(context);
    }

    public ChatBalloon(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ChatBalloon(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		try {
			float density = context.getResources().getDisplayMetrics().density;
			mPointerWidth = Math.round(density * POINTER_WIDTH);
			mPointerHeight = Math.round(density * POINTER_HEIGHT);
	
			/*if (attrs != null) {
				TypedArray a = context.obtainStyledAttributes(attrs, new int[] { R.attr.tint });
				try {
					mColor = a.getColor(R. , 0xffffffff);
				}
				finally {
					a.recycle();
				}
			}*/
			mColor = Color.WHITE;
//			setWillNotCacheDrawing(false);
			mBalloonPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
			mBalloonPaint.setColor(mColor);
			mBalloonPaint.setStyle(Style.FILL_AND_STROKE);
			
//			mBalloonPaint.setShadowLayer(6, 4, 4, 0x80000000);
	
			mPointerPath = new Path();

			//setPadding(getPaddingLeft()+mPointerWidth, getPaddingTop(), getPaddingRight(), getPaddingBottom());
			setOutlineProvider(new ChatBalloneOutlineProvider());
		}
		catch (Exception e) {
			Log.e(TAG, "Error initializing ");
			e.printStackTrace();
		}
	}
	

		
	@Override
	public void draw(Canvas canvas) {
		Log.i(TAG, "draw");
		canvas.translate(mPointerWidth, 0);
		super.draw(canvas);
		canvas.translate(-mPointerWidth, 0);
		canvas.drawPath(mPointerPath, mBalloonPaint);
	}
	
	
	class ChatBalloneOutlineProvider extends ViewOutlineProvider {
		@Override
		public void getOutline(View view, Outline outline) {
			if (mRect.right - mRect.left > mPointerWidth) {
				outline.setConvexPath(mPointerPath);
				outline.setRect(mRect);
				Log.d(TAG, "Setting outline to "+mRect.left+", "+mRect.top+","+mRect.right+","+mRect.bottom);
			}
		}
	}
	
	
	/**
     * Any layout manager that doesn't scroll will want this.
     */
    @Override
    public boolean shouldDelayChildPressedState() {
        return false;
    }

    /**
     * Ask all children to measure themselves and compute the measurement of this
     * layout based on the children.
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int count = getChildCount();

        // These keep track of the space we are using on the left and right for
        // views positioned there; we need member variables so we can also use
        // these for layout later.
        //mLeftWidth = 0;
        //mRightWidth = 0;

        // Measurement will ultimately be computing these values.
        int maxHeight = 0;
        int maxWidth = 0;
        int childState = 0;

        // Iterate through all children, measuring them and computing our dimensions
        // from their size.
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() != GONE) {
                // Measure the child.
            	if (child.getLayoutParams() instanceof MarginLayoutParams) {
            		measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0);
            		// Update our size information based on the layout params.  Children
                    // that asked to be positioned on the left or right go in those gutters.
                    final MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
                    
            		maxWidth = Math.max(maxWidth,
                            child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin);
            		maxHeight = Math.max(maxHeight,
                            child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin);
            		
            	}
            	else {
            		measureChild(child, widthMeasureSpec, heightMeasureSpec);
            		maxWidth = Math.max(maxWidth, child.getMeasuredWidth());
                	maxHeight = Math.max(maxHeight, child.getMeasuredHeight());
            	}

                
                /*if (lp.position == LayoutParams.POSITION_LEFT) {
                    mLeftWidth += Math.max(maxWidth,
                            child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin);
                } else if (lp.position == LayoutParams.POSITION_RIGHT) {
                    mRightWidth += Math.max(maxWidth,
                            child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin);
                } else {*/
//                    maxWidth = Math.max(maxWidth,
//                            child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin);
//                //}
//                maxHeight = Math.max(maxHeight,
//                        child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin);
            	maxHeight = Math.max(maxHeight,
                        child.getMeasuredHeight());
                childState = combineMeasuredStates(childState, child.getMeasuredState());
            }
        }

        // Total width is the maximum width of all inner children plus the gutters.
        //maxWidth += mLeftWidth + mRightWidth;
        maxWidth += mPointerWidth;

        // Check against our minimum height and width
        maxHeight = Math.max(maxHeight, getSuggestedMinimumHeight());
        maxWidth = Math.max(maxWidth, getSuggestedMinimumWidth());

        // Report our final dimensions.
        setMeasuredDimension(resolveSizeAndState(maxWidth, widthMeasureSpec, childState),
                resolveSizeAndState(maxHeight, heightMeasureSpec,
                        childState << MEASURED_HEIGHT_STATE_SHIFT));
        
        Log.i(TAG, "onMeasure, max:"+maxWidth+", "+maxHeight);
    }
    
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    	super.onSizeChanged(w, h, oldw, oldh);
    	
		mPointerPath.reset();
		int x0 = getPaddingLeft()+mPointerWidth;
		mPointerPath.moveTo(getPaddingLeft(), getPaddingTop());
		mPointerPath.lineTo(x0, getPaddingTop());
		mPointerPath.lineTo(x0, getPaddingTop()+mPointerHeight);
		mPointerPath.close();
		
		mRect.set(x0, getPaddingTop(), getWidth()-getPaddingRight(), getHeight()-getPaddingBottom());
    }

    /** These are used for computing child frames based on their gravity. */
//    private final Rect mTmpContainerRect = new Rect();
//    private final Rect mTmpChildRect = new Rect();
    
    /**
     * Position all children within this layout.
     */
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        final int count = getChildCount();

        // These are the far left and right edges in which we are performing layout.
        int leftPos = getPaddingLeft();
        int rightPos = right - left - getPaddingRight();

        // This is the middle region inside of the gutter.
        final int middleLeft = leftPos;//+ mPointerWidth;
        final int middleRight = rightPos;

        // These are the top and bottom edges in which we are performing layout.
        final int parentTop = getPaddingTop();
        final int parentBottom = bottom - top - getPaddingBottom();

        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() != GONE) {

//                final int width = child.getMeasuredWidth();
//                final int height = child.getMeasuredHeight();

//                if (child.getLayoutParams() instanceof MarginLayoutParams) {
//                	final MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
                // Compute the frame in which we are placing this child.
                /*if (lp.position == LayoutParams.POSITION_LEFT) {
                    mTmpContainerRect.left = leftPos + lp.leftMargin;
                    mTmpContainerRect.right = leftPos + width + lp.rightMargin;
                    leftPos = mTmpContainerRect.right;
                } else if (lp.position == LayoutParams.POSITION_RIGHT) {
                    mTmpContainerRect.right = rightPos - lp.rightMargin;
                    mTmpContainerRect.left = rightPos - width - lp.leftMargin;
                    rightPos = mTmpContainerRect.left;
                } else {*/
//                    mTmpContainerRect.left = middleLeft + lp.leftMargin;
//                    mTmpContainerRect.right = middleRight - lp.rightMargin;
                //}
//                mTmpContainerRect.top = parentTop+ lp.topMargin;
//                mTmpContainerRect.bottom = parentBottom - lp.bottomMargin;
//                }

                // Use the child's gravity and size to determine its final
                // frame within its container.
                //Gravity.apply(lp.gravity, width, height, mTmpContainerRect, mTmpChildRect);

                // Place the child.
//                child.layout(mTmpChildRect.left, mTmpChildRect.top,
//                        mTmpChildRect.right, mTmpChildRect.bottom);
            	
            	child.layout(middleLeft, parentTop, middleRight, parentBottom);
            }
        }
    }
	
	
}
