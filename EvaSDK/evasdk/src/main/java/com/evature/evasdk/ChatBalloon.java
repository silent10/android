package com.evature.evasdk;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;

import com.evature.evasdk.util.DLog;


// based on http://developer.android.com/reference/android/view/ViewGroup.html
// https://developer.android.com/training/material/shadows-clipping.html

/*****
 * This is a custom made View in the shape of a simple Chat Balloon  (aka chat bubble)
 * Balloon pointer can point either left or right, and has width and height 
 * 
 * The ChatBalloon expects a single child - If you wish to have more than one then make that child a viewGroup itself, eg. LinearLayout 
 * 
 * @author iftah
 *
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class ChatBalloon extends ViewGroup {

	private static final String TAG = "ChatBalloon";
	private int mColor;
	private static float BORDER_RAD = 5;
	private static int POINTER_WIDTH = 20; // in Device independant pixels
	private static int POINTER_HEIGHT = 15; // in Device independant pixels
	private int mPointerWidth;
	private int mPointerHeight;
	private Paint mBalloonPaint;
	private Path mPointerPath;
	private ChatPointer pointerView;
	private float mDensity;
	public enum PointerSide {
		Left,
		Right
	}
	
	private PointerSide mPointerSide;

	/** These are used for computing child frames based on their gravity. */
    private final Rect mTmpContainerRect = new Rect();
    private Rect mTmpChildRect = new Rect();
    // replace child drawable for border radius
    private GradientDrawable mTmpChildDrawable = new GradientDrawable();    
	

	public ChatBalloon(Context context) {
        super(context);
    }

    public ChatBalloon(Context context, AttributeSet attrs) throws Exception {
        this(context, attrs, 0);
    }

    public ChatBalloon(Context context, AttributeSet attrs, int defStyle) throws Exception {
		super(context, attrs, defStyle);
		
		try {
			mDensity = context.getResources().getDisplayMetrics().density;
	
			if (attrs != null) {
				TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ChatBalloon, 0, defStyle);
				try {
					mColor = a.getColor(R.styleable.ChatBalloon_pointerColor , 0xffffffff);
					mPointerWidth = a.getDimensionPixelSize(R.styleable.ChatBalloon_pointerWidth, Math.round(mDensity * POINTER_WIDTH));
					mPointerHeight = a.getDimensionPixelSize(R.styleable.ChatBalloon_pointerHeight, Math.round(mDensity * POINTER_HEIGHT));
					String pointerSide = a.getString(R.styleable.ChatBalloon_pointerSide);
					if (pointerSide != null) {
						try {
							mPointerSide = PointerSide.valueOf(pointerSide);
						} catch(IllegalArgumentException e) {
							mPointerSide = PointerSide.Left;
							DLog.w(TAG, "PointerSide " + pointerSide + " isn't allowed, valid values are ChatBalloon.PointerSide enum");
						}
					}
				}
				finally {
					a.recycle();
				}
			}
//			setWillNotCacheDrawing(false);
			mBalloonPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
			mBalloonPaint.setColor(mColor);
			mBalloonPaint.setStyle(Style.FILL_AND_STROKE);
			
			mPointerPath = new Path();
			calculatePointerPath();
			pointerView = new ChatPointer(context, attrs, defStyle);
			this.setElevation(getElevation()); // init the pointer elevation and padding/margin offset
			addView(pointerView);
			
			setOutlineProvider(new ChatBalloonOutlineProvider());
		}
		catch (Exception e) {
			DLog.e(TAG, "Error initializing ");
			e.printStackTrace();
			String t = DLog.getStackTraceString(e.getCause());
			DLog.e(TAG, "Cause: "+t);
			throw e;
		}
	}
	

    @Override public void setElevation(float elevation) {
    	super.setElevation(elevation);
    	if (pointerView != null)
    		pointerView.setElevation(elevation + 0.1f);
    	if (mPointerPath != null)
    		calculatePointerPath();
//    	setPadding(getPaddingLeft()+Math.round(getElevation()), getPaddingTop(), getPaddingRight(), getPaddingBottom());
//		ViewGroup.LayoutParams lp = this.getLayoutParams();
//		if (lp instanceof MarginLayoutParams) {
//			MarginLayoutParams mlp = (MarginLayoutParams) lp;
//			mlp.setMarginStart(mlp.getMarginStart() - Math.round(getElevation()));
//		}
    };
		
	
	class ChatPointer extends View {

		public ChatPointer(Context context, AttributeSet attrs, int defStyleAttr) {
			super(context, attrs, defStyleAttr);
			
			setOutlineProvider(new ChatPointerOutlineProvider());
		}
		
		@Override
		public void onDraw(Canvas canvas) {
			super.onDraw(canvas);
			canvas.drawPath(mPointerPath, mBalloonPaint);
			if (mPointerSide == PointerSide.Left) {
				canvas.drawRect(mTmpChildRect.left, mTmpChildRect.top, mTmpChildRect.left+6*mDensity, mTmpChildRect.top+mPointerHeight, mBalloonPaint);
			}
			else {
				canvas.drawRect(0, mTmpChildRect.top, getElevation(), mTmpChildRect.top+mPointerHeight, mBalloonPaint);
			}
		}
		
	}
	
	class ChatPointerOutlineProvider extends ViewOutlineProvider {

		@Override
		public void getOutline(View view, Outline outline) {
			outline.setConvexPath(mPointerPath);
		}
	}
	
	class ChatBalloonOutlineProvider extends ViewOutlineProvider {
		@Override
		public void getOutline(View view, Outline outline) {
			ChatBalloon cb = (ChatBalloon) view;
//			LinearLayout ll = (LinearLayout)cb.getChildAt(1);
//			TextView tv = (TextView) ll.getChildAt(0);
//			String dbgText = tv.getText().subSequence(0, Math.max(tv.getText().length()-1, 20)).toString().replace("\n", "");
//			Log.d(TAG, "Setting : ["+dbgText+"] outline to "+cb.mTmpChildRect.toShortString());
			outline.setRoundRect(cb.mTmpChildRect, BORDER_RAD*mDensity);
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
            if (child.getVisibility() != GONE && child != pointerView) {
                // Measure the child.
            	if (child.getLayoutParams() instanceof MarginLayoutParams) {
            		measureChildWithMargins(child, widthMeasureSpec, mPointerWidth+Math.round(getElevation()), heightMeasureSpec, 0);
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
            	
                childState = combineMeasuredStates(childState, child.getMeasuredState());
            }
        }

        // Total width is the maximum width of all inner children plus the gutters.
        //maxWidth += mLeftWidth + mRightWidth;
//        if (mPointerSide == PointerSide.Left) {
        	maxWidth += mPointerWidth+ Math.round(getElevation());
//        }
//        else {
//        	maxWidth -= mPointerWidth+ Math.round(getElevation());
//        }

        // Check against our minimum height and width
        maxHeight = Math.max(maxHeight, getSuggestedMinimumHeight());
        maxWidth = Math.max(maxWidth, getSuggestedMinimumWidth());

        // Report our final dimensions.
        setMeasuredDimension(resolveSizeAndState(maxWidth, widthMeasureSpec, childState),
                resolveSizeAndState(maxHeight, heightMeasureSpec,
                        childState << MEASURED_HEIGHT_STATE_SHIFT));
        
    }
    
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    	super.onSizeChanged(w, h, oldw, oldh);
    	
		calculatePointerPath();
		
		//mRect.set(x0, getPaddingTop(), w-getPaddingRight(), h-getPaddingBottom());
    }

	private void calculatePointerPath() {
		mPointerPath.reset();
		int y0 = 0;// getPaddingTop();
		if (mPointerSide == PointerSide.Left) {
			int x0 = getPaddingLeft()+Math.round(getElevation());
			mPointerPath.moveTo(x0, y0);
			mPointerPath.lineTo(x0+mPointerWidth, y0);
			mPointerPath.lineTo(x0+mPointerWidth, y0+mPointerHeight);
			mPointerPath.close();
		}
		else {
			int x0 = Math.round(getElevation());
			mPointerPath.moveTo(x0, y0);
			mPointerPath.lineTo(x0+mPointerWidth, y0);
			mPointerPath.lineTo(x0, y0+mPointerHeight);
			mPointerPath.close();
		}
	}
	
	@Override public void setTranslationZ(float translationZ) {
		super.setTranslationZ(translationZ);
		if (translationZ != getTranslationZ()) {
			invalidateOutline();
		}
	};

    /**
     * Position all children within this layout.
     */
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        final int count = getChildCount();

        // These are the far left and right edges in which we are performing layout.
        int leftPos = getPaddingLeft();
        int rightPos = right - left - getPaddingRight();

        // This is the middle region - next to the pointer.
        final int middleLeft;
        if (mPointerSide == PointerSide.Left) { 
        	middleLeft = leftPos+ mPointerWidth+Math.round(getElevation());
        }
        else {
        	middleLeft = leftPos;
        }
        final int middleRight; 
        if (mPointerSide == PointerSide.Left) {
        	middleRight = rightPos;
        }
        else {
        	middleRight = rightPos - mPointerWidth- Math.round(getElevation());
        }

        // These are the top and bottom edges in which we are performing layout.
        final int parentTop = getPaddingTop();
        final int parentBottom = bottom - top - getPaddingBottom();

        if (mPointerSide == PointerSide.Left) {
        	pointerView.layout(leftPos, parentTop, middleLeft+Math.round(getElevation()), parentTop+mPointerHeight+Math.round(getElevation()));
        }
        else {
        	pointerView.layout(middleRight-Math.round(getElevation()), parentTop, rightPos, parentTop+mPointerHeight+Math.round(getElevation()));
        }
        boolean foundNonPointer = false;
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() != GONE && child != pointerView) {
            	if (foundNonPointer) {
            		DLog.w(TAG, "ChatBalloon expected only a single non-pointer child!");
            	}
            	foundNonPointer = true;
                final int width = child.getMeasuredWidth();
                final int height = child.getMeasuredHeight();

                	final LayoutParams lp = (LayoutParams) child.getLayoutParams();
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
                    mTmpContainerRect.left = middleLeft + lp.leftMargin;
                    mTmpContainerRect.right = middleRight - lp.rightMargin;
                //}
                mTmpContainerRect.top = parentTop+ lp.topMargin;
                mTmpContainerRect.bottom = parentBottom - lp.bottomMargin;

                // Use the child's gravity and size to determine its final
                // frame within its container.
                Gravity.apply(lp.gravity, width, height, mTmpContainerRect, mTmpChildRect);

                // Place the child.
                child.layout(mTmpChildRect.left, mTmpChildRect.top,
                        mTmpChildRect.right, mTmpChildRect.bottom);
                if (child.getBackground() != mTmpChildDrawable) {
	                mTmpChildDrawable.setColor(mColor);
	                float borderRad = BORDER_RAD * mDensity;
	                if (mPointerSide == PointerSide.Left) {
	                	mTmpChildDrawable.setCornerRadii(new float[] {0,0, borderRad,borderRad,  borderRad,borderRad,  borderRad,borderRad});
	                }
	                else {
	                	mTmpChildDrawable.setCornerRadii(new float[] {borderRad,borderRad,  0,0, borderRad,borderRad,  borderRad,borderRad});
	                }
	                child.setBackground(mTmpChildDrawable);
                }
                            	
            }
        }
        if (!foundNonPointer) {
        	DLog.w(TAG, "ChatBalloon expected a visible non-pointer child!");
        }
        invalidateOutline();
    }
    
    
 // ----------------------------------------------------------------------
    // The rest of the implementation is for custom per-child layout parameters.
    // If you do not need these (for example you are writing a layout manager
    // that does fixed positioning of its children), you can drop all of this.

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
    }

    @Override
    protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new LayoutParams(p);
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams;
    }

    /**
     * Custom per-child layout information.
     */
    public static class LayoutParams extends MarginLayoutParams {
        /**
         * The gravity to apply with the View to which these layout parameters
         * are associated.
         */
        public int gravity = Gravity.TOP | Gravity.START;


        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);

            // Pull the layout param values from the layout XML during
            // inflation.  This is not needed if you don't care about
            // changing the layout behavior in XML.
            TypedArray a = c.obtainStyledAttributes(attrs, R.styleable.ChatBalloonLP);
            gravity = a.getInt(R.styleable.ChatBalloonLP_android_layout_gravity, gravity);
            a.recycle();
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }
    }
	
	
}
