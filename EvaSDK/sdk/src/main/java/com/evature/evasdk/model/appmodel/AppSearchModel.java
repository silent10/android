package com.evature.evasdk.model.appmodel;

import android.content.Context;

import java.io.Serializable;

/**
 * Created by iftah on 6/2/15.
 */
public abstract class AppSearchModel implements Serializable {
    protected boolean isComplete; // true if this search is "complete" - contains all the mandatory fields

    public abstract void triggerSearch(Context context);
}
