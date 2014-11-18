package com.virtual_hotel_agent.search.views.adapters;


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
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.nhaarman.listviewanimations.appearance.OnAnimEndCallback;
import com.nhaarman.listviewanimations.appearance.SingleAnimationAdapter;
import com.nhaarman.listviewanimations.util.AdapterViewUtil;
import com.nhaarman.listviewanimations.util.ListViewWrapper;
import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.OnDismissCallback;
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
public class ChatAnimAdapter extends SingleAnimationAdapter {

    private static final String TRANSLATION_X = "translationX";
    private final long mAnimationDelayMillis;
    private final long mAnimationDurationMillis;
    private final OnDismissCallback mCallback;
    
    /***
     * @param callback
     *            The {@link OnDismissCallback} to trigger when the user has
     *            indicated that she would like to dismiss one or more list
     *            items.
     */
    public ChatAnimAdapter(final BaseAdapter baseAdapter, final OnDismissCallback callback, final OnAnimEndCallback animEndCallback) {
        this(baseAdapter, 100, 300, callback, animEndCallback);
        
    }

    public ChatAnimAdapter(final BaseAdapter baseAdapter, final long animationDelayMillis, final OnDismissCallback callback, final OnAnimEndCallback animEndCallback) {
        this(baseAdapter, animationDelayMillis, 300, callback, animEndCallback);
    }

    public ChatAnimAdapter(final BaseAdapter baseAdapter, final long animationDelayMillis, final long animationDurationMillis, final OnDismissCallback callback, final OnAnimEndCallback animEndCallback) {
        super(baseAdapter, animEndCallback);
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
    protected Animator getAnimator(final ViewGroup parent, final View view) {
    	ChatItem chatItem = (ChatItem) view.getTag();
    	if (chatItem != null && chatItem.getType() == ChatType.Me) {
    		Animator result = ObjectAnimator.ofFloat(view, TRANSLATION_X, 0 - parent.getWidth(), 0);
    		result.setStartDelay(0);
    		return result;
    	}
    	else {
    		Animator result = ObjectAnimator.ofFloat(view, TRANSLATION_X, parent.getWidth(), 0);
    		result.setStartDelay(250);
    		return result;
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
        final List<Integer> positionsCopy = new ArrayList<Integer>(positions);
        if (getListViewWrapper() == null) {
            throw new IllegalStateException("Call getListViewWrapper() on this AnimateDismissAdapter before calling setAdapter()!");
        }

        List<View> views = getVisibleViewsForPositions(positionsCopy);

        if (!views.isEmpty()) {
            Animator[] animatorsArray = new Animator[views.size()];
            int i=0;
            for (final View view : views) {
            	animatorsArray[i++] = createAnimatorForView(view);
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

    private Animator createAnimatorForView(final View view) {
        final ViewGroup.LayoutParams lp = view.getLayoutParams();
        final int originalHeight = view.getHeight();

        ValueAnimator animator = ValueAnimator.ofInt(originalHeight, 0);
        animator.addListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationEnd(final Animator animator) {
                lp.height = 0;
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
