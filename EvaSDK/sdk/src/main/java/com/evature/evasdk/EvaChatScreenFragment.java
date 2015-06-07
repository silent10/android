package com.evature.evasdk;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by iftah on 6/4/15.
 */
public class EvaChatScreenFragment extends Fragment {
    EvaChatScreenComponent chatScreen;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        chatScreen = new EvaChatScreenComponent(getActivity());
        chatScreen.onCreate(savedInstanceState);
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

/*    @Override
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
        chatScreen.onDestroy();
        super.onDestroy();
    }

}