package com.evature.evasdk;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import com.evature.evasdk.appinterface.AppScope;
import com.evature.evasdk.evaapis.android.EvaSpeak;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * Created by iftah on 5/26/15.
 */
public class EvaButton {

    private static final String TAG = "EvaButton";
    public static ArrayList<WeakReference<ImageButton>> evaButtons = new ArrayList<WeakReference<ImageButton>>();
    private static float MARGIN_BOTTOM = 24;  // margin in DIP

    public static void addDefaultButton(FragmentActivity activity) {
        addDefaultButton(activity, null);
    }

    public static void addDefaultButton(FragmentActivity activity, final AppScope evaContext) {
        // initialize TTS
        EvaSpeak.getOrCreateInstance(activity.getApplicationContext());

        ImageButton searchButton = (ImageButton) LayoutInflater.from(activity).inflate(R.layout.voice_search_button, null);
        WeakReference<ImageButton> weakRef = new WeakReference<ImageButton>(searchButton);
        evaButtons.add(weakRef);
        final FragmentActivity fActivity = activity;
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSearchByVoice(fActivity, evaContext);
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

        ViewGroup rootView = getOrCreateRootView(activity);

        rootView.addView(searchButton);

    }

    private static ViewGroup getOrCreateRootView(FragmentActivity activity) {
        View rootView = activity.findViewById(R.id.evature_root_view);
        if (rootView == null) {
            EvaSpeak.getOrCreateInstance(activity.getApplicationContext());

            RelativeLayout rl = new RelativeLayout(activity);
            rl.setId(R.id.evature_root_view);
            rl.setGravity(Gravity.BOTTOM);
            activity.getWindow().addContentView(rl, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
        }
        return (ViewGroup) rootView;
    }

    public static void startSearchByVoice(FragmentActivity activity, AppScope evaContext) {
        final FragmentManager manager = activity.getSupportFragmentManager();

        View rootView = getOrCreateRootView(activity);

        Fragment newFragment = new EvaChatScreenFragment();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out, android.R.anim.fade_in, android.R.anim.fade_out);
        transaction.add(R.id.evature_root_view, newFragment, EvaChatScreenFragment.TAG);
        transaction.addToBackStack(null);
        transaction.commit();
    }


//    public static void startSearchByVoiceActivity( FragmentActivity activity, AppScope evaContext) {
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                    Window window = activity.getWindow();
//                    window.addFlags(Window.FEATURE_ACTIVITY_TRANSITIONS);
//                    window.setSharedElementEnterTransition(new ChangeImageTransform());
//                    window.setSharedElementExitTransition(new ChangeImageTransform());
//                    ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(activity, searchButton, "eva_microphone_button");
//                    Intent intent = new Intent(activity, EvaChatScreenComponent.class);
//                    activity.startActivity(intent, options.toBundle());
//                }
//                else {
//
//        Intent intent = new Intent(activity, EvaChatScreenActivity.class);
//        if (evaContext != null) {
//            intent.putExtra(EvaChatScreenComponent.INTENT_EVA_CONTEXT, evaContext.toString());
//        }
//        activity.startActivity(intent);
//    }
}
