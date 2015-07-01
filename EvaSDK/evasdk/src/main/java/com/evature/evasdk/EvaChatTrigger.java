package com.evature.evasdk;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import com.evature.evasdk.appinterface.AppScope;
import com.evature.evasdk.evaapis.EvaSpeak;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * Created by iftah on 5/26/15.
 */
public class EvaChatTrigger {

    private static final String TAG = "EvaChatTrigger";
    public static ArrayList<WeakReference<ImageButton>> evaButtons = new ArrayList<WeakReference<ImageButton>>();
    private static float MARGIN_BOTTOM = 24;  // margin in DIP

    public static void addDefaultButton(FragmentActivity activity) {
        addDefaultButton(activity, null);
    }

    public static void addDefaultButton(FragmentActivity activity, final AppScope evaContext) {
        // initialize TTS
        EvaSpeak.getOrCreateInstance(activity.getApplicationContext());

        ImageButton searchButton = (ImageButton) LayoutInflater.from(activity).inflate(R.layout.evature_voice_search_button, null);
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

            final TypedArray styledAttributes = activity.getTheme().obtainStyledAttributes(
                    new int[] { android.R.attr.actionBarSize });
            int actionBarSize = (int) styledAttributes.getDimension(0, 0);
            styledAttributes.recycle();

            RelativeLayout rl = new RelativeLayout(activity);
            rl.setId(R.id.evature_root_view);
            rl.setGravity(Gravity.BOTTOM);
            rl.setPadding(0, actionBarSize, 0, 0);
            activity.getWindow().addContentView(rl, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
            rootView = rl;
        }
        return (ViewGroup) rootView;
    }

    public static void startSearchByVoice(FragmentActivity activity) {
        startSearchByVoice(activity, null);
    }
    public static void startSearchByVoice(FragmentActivity activity, AppScope evaContext) {
        final FragmentManager manager = activity.getSupportFragmentManager();

        getOrCreateRootView(activity);

        Intent theIntent = activity.getIntent();
        if (evaContext != null) {
            theIntent.putExtra(EvaChatScreenComponent.INTENT_EVA_CONTEXT, evaContext.toString());
        }

        Fragment newFragment = new EvaChatScreenFragment();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out, android.R.anim.fade_in, android.R.anim.fade_out);
        transaction.add(R.id.evature_root_view, newFragment, EvaChatScreenFragment.TAG);
        transaction.addToBackStack(null);
        transaction.commit();
    }


    public static void closeChatScreen(Context activity) {
        FragmentActivity fa = (FragmentActivity)activity;
        FragmentManager manager = fa.getSupportFragmentManager();
        Fragment fragment = manager.findFragmentByTag(EvaChatScreenFragment.TAG);
        EvaChatScreenFragment evaChatScreenFragment = (EvaChatScreenFragment) fragment;
        evaChatScreenFragment.closeChatFragment();
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
