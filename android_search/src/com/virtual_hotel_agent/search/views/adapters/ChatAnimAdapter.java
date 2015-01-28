package com.virtual_hotel_agent.search.views.adapters;


import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.evature.util.DLog;
import com.nhaarman.listviewanimations.appearance.AnimationAdapter;
import com.nhaarman.listviewanimations.appearance.OnAnimEndCallback;
import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.OnDismissCallback;
import com.nhaarman.listviewanimations.util.AdapterViewUtil;
import com.nhaarman.listviewanimations.util.ListViewWrapper;
import com.virtual_hotel_agent.search.models.chat.ChatItem;
import com.virtual_hotel_agent.search.models.chat.ChatItem.ChatType;

/**
 * An implementation of the AnimationAdapter class which applies a
 * swing-in-from-the-right-animation to VHA-chats, and from the left to my-chats
 * also
 * dismiss chatitem gets animation
 * 
 * Based on AnimateDismissAdapter and SingleAnimationAdapter
 */
public class ChatAnimAdapter extends AnimationAdapter {

	private static final String TAG = "ChatAnimAdapter";
    private static final String TRANSLATION_X = "translationX";
    private static final String TRANSLATION_Y = "translationY";
    private static final String TRANSLATION_Z = "translationZ";
    private final long mAnimationDelayMillis;
    private final long mAnimationDurationMillis;
    private final OnDismissCallback mCallback;
    
    /***
     * @param callback
     *            The {@link OnDismissCallback} to trigger when the user has
     *            indicated that she would like to dismiss one or more list
     *            items.
     */
//    public ChatAnimAdapter(final BaseAdapter baseAdapter, final OnDismissCallback callback, final OnAnimEndCallback animEndCallback) {
//        this(baseAdapter, 100, 300, callback, animEndCallback);
//        
//    }
//
//    public ChatAnimAdapter(final BaseAdapter baseAdapter, final long animationDelayMillis, final OnDismissCallback callback, final OnAnimEndCallback animEndCallback) {
//        this(baseAdapter, animationDelayMillis, 300, callback, animEndCallback);
//    }

    public ChatAnimAdapter(final BaseAdapter baseAdapter, final long animationDelayMillis, final long animationDurationMillis, final OnDismissCallback callback, final OnAnimEndCallback animEndCallback) {
        super(baseAdapter, new OnAnimEndCallback() {
			
			@Override
			public void onAnimEnd(View view) {
				view.setScaleY(1.0f);
				view.setScaleX(1.0f);
				if (animEndCallback != null) {
					animEndCallback.onAnimEnd(view);
				}
			}
		});
        mAnimationDelayMillis = animationDelayMillis;
        mAnimationDurationMillis = animationDurationMillis;
        mCallback = callback;
    }

    
    @Override
    protected void animateViewIfNecessary(int position, View view, ViewGroup parent) {
    	if (view.getTag() == null)
    		return;
//    	if (position == getCount() -1) {
//    		// last position is a filler row - no animate
//    		return;
//    	}
    	super.animateViewIfNecessary(position, view, parent);
    };
       
    @Override
    public Animator[] getAnimators(final ViewGroup parent, final View view, final int position) {
    	ChatItem chatItem = (ChatItem) view.getTag();
    	
    	final View chatBalloon = ((ViewGroup)view).getChildAt(0); // get the cHat 
//    		Animator heightAnim = createHeightAnimatorForView(view, 0.1f, 1.0f);
//		Animator scaleYAnim = ObjectAnimator.ofFloat(chatBalloon, "scaleY", 1.1f, 1.0f);
//		Animator scaleXAnim = ObjectAnimator.ofFloat(chatBalloon, "scaleX", 1.1f, 1.0f);
    	Animator heightAnim = ObjectAnimator.ofFloat(chatBalloon, TRANSLATION_Z, 12f, 0f);
    	Animator yAnim = ObjectAnimator.ofFloat(chatBalloon, TRANSLATION_Y, -12f, 0f);
		//view.setScaleY(0.1f);
    	chatBalloon.setTranslationZ(12f);
    	chatBalloon.setTranslationY(-12f);
    	chatBalloon.setScaleX(1.1f);
    	chatBalloon.setScaleY(1.1f);
		heightAnim.setStartDelay(mAnimationDurationMillis+mAnimationDelayMillis);
		heightAnim.setDuration(mAnimationDurationMillis);
//		scaleXAnim.setStartDelay(mAnimationDurationMillis+mAnimationDelayMillis);
//		scaleXAnim.setDuration(mAnimationDurationMillis);
//		scaleYAnim.setStartDelay(mAnimationDurationMillis+mAnimationDelayMillis);
//		scaleYAnim.setDuration(mAnimationDurationMillis);
		yAnim.setStartDelay(mAnimationDurationMillis+mAnimationDelayMillis);
		yAnim.setDuration(mAnimationDurationMillis);

    	if (chatItem != null && chatItem.getType() == ChatType.Me) {
    		
    		Animator translateX = ObjectAnimator.ofFloat(view, TRANSLATION_X, 0 - parent.getWidth(), 0);
    		translateX.setStartDelay(0);
    		return new Animator[] {translateX, //scaleYAnim, scaleXAnim, 
    				yAnim, heightAnim};
    	}
    	else {
    		Animator translateX = ObjectAnimator.ofFloat(view, TRANSLATION_X, parent.getWidth(), 0);
    		translateX.setStartDelay(250);
    		return new Animator[]  {translateX, //scaleYAnim, scaleXAnim, 
    				yAnim, heightAnim};
    	}
    }
    
    
    
    /************** Code for dismissing items ******************/
    
    /**
     * Animate dismissal of the item at given position.
     */
    @SuppressWarnings("UnusedDeclaration")
    public void animateDismiss(final int position) {
        animateDismiss(Arrays.asList(position));
    }

    /**
     * Animate dismissal of the items at given positions.
     */
    public void animateDismiss(final Collection<Integer> positions) {
        if (getListViewWrapper() == null) {
            throw new IllegalStateException("Call getListViewWrapper() on this AnimateDismissAdapter before calling setAdapter()!");
        }
        final List<Integer> positionsCopy = new ArrayList<Integer>(positions);

        List<View> views = getVisibleViewsForPositions(positionsCopy);

        if (!views.isEmpty()) {
            Animator[] animatorsArray = new Animator[views.size()];
            int i=0;
            for (final View view : views) {
            	animatorsArray[i++] = createHeightAnimatorForView(view, 1.0f, 0f);
            }

            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.setDuration(mAnimationDurationMillis);
            animatorSet.setStartDelay(mAnimationDelayMillis);
            animatorSet.playTogether(animatorsArray);
            animatorSet.addListener(new AnimatorListenerAdapter() {

                @Override
                public void onAnimationEnd(final Animator animator) {
                    invokeCallback(positionsCopy);
                }
            });
            animatorSet.start();
        } else {
            invokeCallback(positionsCopy);
        }
    }

    private void invokeCallback(final Collection<Integer> positions) {
        ArrayList<Integer> positionsList = new ArrayList<Integer>(positions);
        Collections.sort(positionsList);

        int[] dismissPositions = new int[positionsList.size()];
        for (int i = 0; i < positionsList.size(); i++) {
            dismissPositions[i] = positionsList.get(positionsList.size() - 1 - i);
        }
        mCallback.onDismiss(getListViewWrapper().getListView(), dismissPositions);
    }

    private List<View> getVisibleViewsForPositions(final Collection<Integer> positions) {
        List<View> views = new ArrayList<View>();
        ListViewWrapper listView = getListViewWrapper();
        for (int i = 0; i < listView.getChildCount(); i++) {
            View child = listView.getChildAt(i);
            if (positions.contains(AdapterViewUtil.getPositionForView(listView, child))) {
                views.add(child);
            }
        }
        return views;
    }

    private Animator createHeightAnimatorForView(final View view, final float start, final float end) {
//        try {
//            Method m = view.getClass().getDeclaredMethod("onMeasure", int.class, int.class);
//            m.setAccessible(true);
//            m.invoke(
//                view,
//                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
//                MeasureSpec.makeMeasureSpec(((View)view.getParent()).getMeasuredHeight(), MeasureSpec.AT_MOST)
//            );
//        } catch (Exception e){
//            Log.e("test", "", e);
//        }
//        final int originalHeight = view.getMeasuredHeight();
//        Log.d(TAG, "initialHeight="+originalHeight);
    	final ViewGroup.LayoutParams lp = view.getLayoutParams();
    	final int originalHeight = view.getHeight();
    	DLog.d(TAG, "initialHeight="+originalHeight);
        ValueAnimator animator = ValueAnimator.ofInt(Math.round(originalHeight*start), Math.round(originalHeight*end));
        animator.addListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationEnd(final Animator animator) {
                lp.height = Math.round(originalHeight*end);
                view.setLayoutParams(lp);
            }
        });

        animator.addUpdateListener(new AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(final ValueAnimator valueAnimator) {
                lp.height = (Integer) valueAnimator.getAnimatedValue();
                view.setLayoutParams(lp);
            }
        });

        return animator;
    }
}
