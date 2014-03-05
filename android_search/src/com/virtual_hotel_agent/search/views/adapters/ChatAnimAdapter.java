package com.virtual_hotel_agent.search.views.adapters;


import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.nhaarman.listviewanimations.swinginadapters.SingleAnimationAdapter;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.ObjectAnimator;
import com.virtual_hotel_agent.search.models.chat.ChatItem;
import com.virtual_hotel_agent.search.models.chat.ChatItem.ChatType;

/**
 * An implementation of the AnimationAdapter class which applies a
 * swing-in-from-the-right-animation to views.
 */
public class ChatAnimAdapter extends SingleAnimationAdapter {

    private static final String TRANSLATION_X = "translationX";
    private final long mAnimationDelayMillis;
    private final long mAnimationDurationMillis;

    public ChatAnimAdapter(final BaseAdapter baseAdapter) {
        this(baseAdapter, DEFAULTANIMATIONDELAYMILLIS, DEFAULTANIMATIONDURATIONMILLIS);
    }

    public ChatAnimAdapter(final BaseAdapter baseAdapter, final long animationDelayMillis) {
        this(baseAdapter, animationDelayMillis, DEFAULTANIMATIONDURATIONMILLIS);
    }

    public ChatAnimAdapter(final BaseAdapter baseAdapter, final long animationDelayMillis, final long animationDurationMillis) {
        super(baseAdapter);
        mAnimationDelayMillis = animationDelayMillis;
        mAnimationDurationMillis = animationDurationMillis;
    }

    @Override
    protected long getAnimationDelayMillis() {
        return mAnimationDelayMillis;
    }

    @Override
    protected long getAnimationDurationMillis() {
        return mAnimationDurationMillis;
    }
    
    @Override
    protected void animateViewIfNecessary(int position, View view, ViewGroup parent) {
    	if (position == getCount() -1) {
    		// last position is a filler row - no animate
    		return;
    	}
    	super.animateViewIfNecessary(position, view, parent);
    };
    
    @Override
    protected Animator getAnimator(final ViewGroup parent, final View view) {
    	ChatItem chatItem = (ChatItem) view.getTag();
    	if (chatItem != null && chatItem.getType() == ChatType.Me) {
    		return ObjectAnimator.ofFloat(view, TRANSLATION_X, 0 - parent.getWidth(), 0);
    	}
    	else {
    		return ObjectAnimator.ofFloat(view, TRANSLATION_X, parent.getWidth(), 0);
    	}
    }
}
