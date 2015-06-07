package com.evature.evasdk;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/**
 * Created by iftah on 6/4/15.
 */
public class EvaChatScreenActivity extends Activity {
    EvaChatScreenComponent chatScreen;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        chatScreen = new EvaChatScreenComponent(this);
        chatScreen.onCreate(savedInstanceState);
        setContentView(chatScreen.createMainView(getLayoutInflater(), null, savedInstanceState));
    }

    @Override
    protected void onResume() {
        super.onResume();
        chatScreen.onResume();

        Intent intent = getIntent();
        if ("com.google.android.gms.actions.SEARCH_ACTION".equals(intent.getAction())) {
            onNewIntent(new Intent());
        }
    }

    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        chatScreen.onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        // getIntent() should always return the most recent
        setIntent(intent);
    }

    @Override
    protected void onPause() {
        chatScreen.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        chatScreen.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (!chatScreen.onBackPressed()) {
            super.onBackPressed();
        }
    }
}