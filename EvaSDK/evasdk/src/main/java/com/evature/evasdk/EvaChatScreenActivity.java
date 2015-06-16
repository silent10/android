package com.evature.evasdk;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.view.MenuItem;

import com.evature.evasdk.util.DLog;

/**
 * Created by iftah on 6/4/15.
 */
public class EvaChatScreenActivity extends Activity {
    private static final String TAG = "EvaChatScreenActivity";
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

            // back pressed, not handled internally, try to see if parent task should be created
            Intent upIntent = NavUtils.getParentActivityIntent(this);
            if (upIntent == null) {
                super.onBackPressed();
                return;
            }

            if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
                // This activity is NOT part of this app's task, so create a new task
                // when navigating up, with a synthesized back stack.
                TaskStackBuilder.create(this)
                        // Add all of this activity's parents to the back stack
                        .addNextIntentWithParentStack(upIntent)
                                // Navigate up to the closest parent
                        .startActivities();
            } else {
                // This activity is part of this app's task, so simply
                // navigate up to the logical parent activity.
                NavUtils.navigateUpTo(this, upIntent);
            }
        }
    }
}