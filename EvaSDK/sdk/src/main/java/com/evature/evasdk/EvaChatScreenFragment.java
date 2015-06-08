package com.evature.evasdk;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by iftah on 6/4/15.
 */
public class EvaChatScreenFragment extends Fragment implements  FragmentManager.OnBackStackChangedListener {

    public static final String TAG = "EvaChatScreenFragment";
    EvaChatScreenComponent chatScreen;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        chatScreen = new EvaChatScreenComponent(getActivity(), true);
        chatScreen.onCreate(savedInstanceState);

        final FragmentManager manager = getActivity().getSupportFragmentManager();
        manager.addOnBackStackChangedListener(this);
    }

    public void onBackStackChanged() {
        // Update your UI here.
        final FragmentManager manager = getActivity().getSupportFragmentManager();
        Log.i(TAG, "Backstack changed " + manager.getBackStackEntryCount());
        if (manager.getBackStackEntryCount() == 1) {
            Fragment fragment = manager.findFragmentByTag(TAG);
            if (fragment != null && fragment == EvaChatScreenFragment.this) {
                EvaChatScreenFragment evaChatScreenFragment = (EvaChatScreenFragment) fragment;
                boolean handledInternally = evaChatScreenFragment.chatScreen.onBackPressed();
            }
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return chatScreen.createMainView(inflater, container, savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();

        chatScreen.onResume();

        Intent intent = getActivity().getIntent();
        if ("com.google.android.gms.actions.SEARCH_ACTION".equals(intent.getAction())) {
            //getActivity().onNewIntent(new Intent());
            getActivity().setIntent(new Intent());
        }
    }

    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        chatScreen.onSaveInstanceState(savedInstanceState);
    }

/*  @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        // getIntent() should always return the most recent
        setIntent(intent);
    }
*/
    @Override
    public void onPause() {
        chatScreen.onPause();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        FragmentManager manager = getActivity().getSupportFragmentManager();
        manager.removeOnBackStackChangedListener(this);
        chatScreen.onDestroy();
        super.onDestroy();
    }

}