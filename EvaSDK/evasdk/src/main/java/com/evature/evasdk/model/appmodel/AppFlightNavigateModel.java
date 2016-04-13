package com.evature.evasdk.model.appmodel;

import android.content.Context;

import com.evature.evasdk.appinterface.CallbackResult;
import com.evature.evasdk.appinterface.FlightNavigate;
import com.evature.evasdk.appinterface.HotelSearch;
import com.evature.evasdk.evaapis.EvaComponent;

/**
 * Created by iftah on 12/04/2016.
 */
public class AppFlightNavigateModel extends AppSearchModel {
    private final FlightNavigate.FlightPageType page;

    public AppFlightNavigateModel(FlightNavigate.FlightPageType page) {
        super(true);
        this.page = page;
    }

    public CallbackResult triggerSearch(Context context) {
        if (EvaComponent.evaAppHandler instanceof FlightNavigate) {
            return ((FlightNavigate) EvaComponent.evaAppHandler).navigateTo(context, page);
        }
        return null;
    }
}
