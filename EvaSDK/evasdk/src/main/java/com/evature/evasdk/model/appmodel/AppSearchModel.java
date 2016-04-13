package com.evature.evasdk.model.appmodel;

import android.content.Context;

import com.evature.evasdk.appinterface.CallbackResult;

import java.io.Serializable;

/**
 * Created by iftah on 6/2/15.
 */
public abstract class AppSearchModel implements Serializable {
    protected boolean isComplete; // true if this search is "complete" - contains all the mandatory fields

    public abstract CallbackResult triggerSearch(Context context);

    AppSearchModel(boolean isComplete) {
        this.isComplete = isComplete;
    }

    public void setIsComplete(boolean complete) {
        isComplete = complete;
    }
    public boolean getIsComplete() {return isComplete;}
}
