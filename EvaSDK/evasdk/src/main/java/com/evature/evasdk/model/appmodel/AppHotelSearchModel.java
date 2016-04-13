package com.evature.evasdk.model.appmodel;

import android.content.Context;

import com.evature.evasdk.appinterface.CallbackResult;
import com.evature.evasdk.appinterface.FlightSearch;
import com.evature.evasdk.appinterface.HotelSearch;
import com.evature.evasdk.evaapis.EvaComponent;
import com.evature.evasdk.evaapis.crossplatform.EvaLocation;
import com.evature.evasdk.evaapis.crossplatform.EvaTravelers;
import com.evature.evasdk.evaapis.crossplatform.FlightAttributes;
import com.evature.evasdk.evaapis.crossplatform.HotelAttributes;
import com.evature.evasdk.evaapis.crossplatform.RequestAttributes;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;

/**
 * Created by iftah on 6/2/15.
 */
public class AppHotelSearchModel extends AppSearchModel {

    private final EvaLocation location;

    private final Date arriveDateMin;
    private final Date arriveDateMax;
    private final Integer durationMin;
    private final Integer durationMax;
    private final EvaTravelers travelers;
    private final HotelAttributes attributes;
    private final RequestAttributes.SortEnum sortBy;
    private final RequestAttributes.SortOrderEnum sortOrder;

    /***
     *
     * @param isComplete
     * @param location
     * @param travelers
     * @param sortBy
     * @param sortOrder
     */
    public AppHotelSearchModel(boolean isComplete, EvaLocation location,
                               Date arriveDateMin, Date arriveDateMax,
                               Integer durationMin, Integer durationMax,
                               EvaTravelers travelers,
                               HotelAttributes attributes,
                               RequestAttributes.SortEnum sortBy,
                               RequestAttributes.SortOrderEnum sortOrder) {
        super(isComplete);
        this.location = location;
        this.travelers = travelers;
        this.arriveDateMax = arriveDateMax;
        this.arriveDateMin = arriveDateMin;
        this.durationMax = durationMax;
        this.durationMin = durationMin;
        this.attributes = attributes;
        this.sortBy = sortBy;
        this.sortOrder = sortOrder;
    }

    public CallbackResult triggerSearch(Context context) {
        if (EvaComponent.evaAppHandler instanceof HotelSearch) {
            return ((HotelSearch) EvaComponent.evaAppHandler).handleHotelSearch(context, isComplete,
                    location,
                    arriveDateMin, arriveDateMax,
                    durationMin, durationMax,
                    travelers,
                    attributes,
                    sortBy, sortOrder);
        }
        return null;
    }
}
