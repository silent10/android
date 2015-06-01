package com.evature.evasdk;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.os.Build;
import android.transition.ChangeImageTransform;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * Created by iftah on 5/26/15.
 */
public class EvaButton {

    public static ArrayList<WeakReference<ImageButton>> evaButtons = new ArrayList<WeakReference<ImageButton>>();
    private static float MARGIN_BOTTOM = 24;  // margin in DIP


    public static void addDefaultButton(final Activity activity) {

        ImageButton searchButton = (ImageButton) LayoutInflater.from(activity).inflate(R.layout.voice_search_button, null);
        WeakReference<ImageButton> weakRef = new WeakReference<ImageButton>(searchButton);
        evaButtons.add(weakRef);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //searchButton.setVisibility(View.GONE);
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                    Window window = activity.getWindow();
//                    window.addFlags(Window.FEATURE_ACTIVITY_TRANSITIONS);
//                    window.setSharedElementEnterTransition(new ChangeImageTransform());
//                    window.setSharedElementExitTransition(new ChangeImageTransform());
//                    ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(activity, searchButton, "eva_microphone_button");
//                    Intent intent = new Intent(activity, SearchByVoiceActivity.class);
//                    activity.startActivity(intent, options.toBundle());
//                }
//                else {
                    Intent intent = new Intent(activity, SearchByVoiceActivity.class);
                    activity.startActivity(intent);
//                }

            }
        });
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            searchButton.setTransitionName("eva_microphone_button");
//        }
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        final float scale = activity.getResources().getDisplayMetrics().density;
        int pixels = (int) (MARGIN_BOTTOM * scale + 0.5f);
        params.bottomMargin = pixels;
        searchButton.setLayoutParams(params);
        RelativeLayout rl = new RelativeLayout(activity);
        rl.setGravity(Gravity.BOTTOM);
        rl.addView(searchButton);

        activity.getWindow().addContentView(rl, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
    }

}
