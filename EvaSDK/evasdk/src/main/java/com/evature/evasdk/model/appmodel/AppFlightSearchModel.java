package com.evature.evasdk.model.appmodel;

import android.content.Context;

import com.evature.evasdk.appinterface.EvaResult;
import com.evature.evasdk.appinterface.EvaFlightSearch;
import com.evature.evasdk.evaapis.EvaComponent;
import com.evature.evasdk.evaapis.crossplatform.EvaLocation;
import com.evature.evasdk.evaapis.crossplatform.EvaTravelers;
import com.evature.evasdk.evaapis.crossplatform.FlightAttributes;
import com.evature.evasdk.evaapis.crossplatform.RequestAttributes;

import java.util.Date;

/**
 * Created by iftah on 6/2/15.
 */
public class AppFlightSearchModel extends AppSearchModel {

    private final EvaLocation origin;
    private final EvaLocation destination;
    private final RequestAttributes.SortEnum sortBy;

    private final Date departDateMin;
    private final Date departDateMax;
    private final Date returnDateMin;
    private final Date returnDateMax;
    private final EvaTravelers travelers;
    private final FlightAttributes attributes;
    private final RequestAttributes.SortOrderEnum sortOrder;

    /***
     *
     * @param isComplete
     * @param origin
     * @param destination
     * @param departDateMin
     * @param departDateMax
     * @param returnDateMin
     * @param returnDateMax
     * @param travelers
     * @param attributes
     * @param sortBy
     */
    public AppFlightSearchModel(boolean isComplete, EvaLocation origin, EvaLocation destination,
                                Date departDateMin, Date departDateMax,
                                Date returnDateMin, Date returnDateMax,
                                EvaTravelers travelers,
                                FlightAttributes attributes,
                                RequestAttributes.SortEnum sortBy,
                                RequestAttributes.SortOrderEnum sortOrder) {
        super(isComplete);
        this.origin = origin;
        this.destination = destination;
        this.sortBy = sortBy;
        this.departDateMin = departDateMin;
        this.departDateMax = departDateMax;
        this.returnDateMin = returnDateMin;
        this.returnDateMax = returnDateMax;
        this.travelers = travelers;
        this.attributes = attributes;
        this.sortOrder = sortOrder;
    }

    public EvaResult triggerSearch(Context context) {
        if (EvaComponent.evaAppHandler instanceof EvaFlightSearch) {
            return ((EvaFlightSearch) EvaComponent.evaAppHandler).handleFlightSearch(context,  isComplete,  origin,  destination,
                    departDateMin,  departDateMax,
                    returnDateMin,  returnDateMax,
                    travelers,
                    attributes, sortBy, sortOrder);

        }
        return null;
    }
}
