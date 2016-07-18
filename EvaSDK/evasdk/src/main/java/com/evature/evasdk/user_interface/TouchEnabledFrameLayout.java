package com.evature.evasdk.user_interface;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;

import com.evature.evasdk.util.DLog;

/**
 * Created by iftah on 17/07/2016.
 */
public class TouchEnabledFrameLayout extends FrameLayout {

    public TouchEnabledFrameLayout(Context context) {
        super(context);
    }

    public TouchEnabledFrameLayout(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

    public TouchEnabledFrameLayout(final Context context, final AttributeSet attrs, final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public TouchEnabledFrameLayout(final Context context, final AttributeSet attrs, final int defStyleAttr, final int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
//        DLog.i("TouchEnabledFrameLayout", ">>>> Touch event "+ev);
        return true;
    }
}
