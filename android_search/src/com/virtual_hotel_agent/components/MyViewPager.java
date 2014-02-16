package com.virtual_hotel_agent.components;

import java.lang.reflect.Field;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.animation.DecelerateInterpolator;
import android.widget.Scroller;

import com.evature.util.Log;

public class MyViewPager extends ViewPager   // see http://stackoverflow.com/a/15549865/519995   bug fix for smooth scrolling
{
  public MyViewPager( Context context, AttributeSet attrs) 
  {
    super( context, attrs );
    setMyScroller();
  }
  private void setMyScroller() 
  {
    try 
    {
        Class<?> viewpager = ViewPager.class;
        Field scroller = viewpager.getDeclaredField("mScroller");
        scroller.setAccessible(true);
        scroller.set(this, new MyScroller(getContext()));
    } catch (Exception e) 
    {
        e.printStackTrace();
    }
  }

  public class MyScroller extends Scroller 
  {
    public MyScroller(Context context) 
    {
        super(context, new DecelerateInterpolator());
    }

    @Override
    public void startScroll(int startX, int startY, int dx, int dy, int duration) 
    {
    	Log.d("viewpager", "startx="+startX+" dx="+dx+"  duration="+duration);
    	if (duration < 500 && duration % 100 == 0) {
    		duration = 850;
    	}
        super.startScroll(startX, startY, dx, dy, duration /* milli seconds */);
    }
  }
} 