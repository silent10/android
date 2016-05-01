package com.evature.evasdk.model.appmodel;

import android.content.Context;

import com.evature.evasdk.appinterface.EvaResult;
import com.evature.evasdk.appinterface.EvaFlightNavigate;
import com.evature.evasdk.evaapis.EvaComponent;

/**
 * Created by iftah on 12/04/2016.
 */
public class AppFlightNavigateModel extends AppSearchModel {
    private final EvaFlightNavigate.FlightPageType page;

    public AppFlightNavigateModel(EvaFlightNavigate.FlightPageType page) {
        super(true);
        this.page = page;
    }

    public EvaResult triggerSearch(Context context) {
        if (EvaComponent.evaAppHandler instanceof EvaFlightNavigate) {
            return ((EvaFlightNavigate) EvaComponent.evaAppHandler).navigateTo(context, page);
        }
        return null;
    }
}
