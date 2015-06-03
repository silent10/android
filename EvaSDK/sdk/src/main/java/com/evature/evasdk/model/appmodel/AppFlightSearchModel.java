package com.evature.evasdk.model.appmodel;

import android.content.Context;

import com.evature.evasdk.appinterface.FlightSearch;
import com.evature.evasdk.evaapis.android.EvaComponent;
import com.evature.evasdk.evaapis.crossplatform.EvaLocation;
import com.evature.evasdk.evaapis.crossplatform.FlightAttributes;
import com.evature.evasdk.evaapis.crossplatform.RequestAttributes;

import java.util.Date;

/**
 * Created by iftah on 6/2/15.
 */
public class AppFlightSearchModel extends AppSearchModel {

    public EvaLocation from;
    public EvaLocation to;
    public Date dateFrom;
    public Date dateTo;
    public Integer durationMin;
    public Integer durationMax;
    public FlightAttributes attributes;
    public RequestAttributes.SortEnum sortBy;


    public AppFlightSearchModel(EvaLocation from,
                                EvaLocation to,
                                Date dateFrom,
                                Date dateTo,
                                Integer durationMin,
                                Integer durationMax,
                                FlightAttributes attributes,
                                RequestAttributes.SortEnum sortBy) {
        this.from = from;
        this.to = to;
        this.dateFrom = dateFrom;
        this.dateTo = dateTo;
        this.durationMin = durationMin;
        this.durationMax = durationMax;
        this.attributes = attributes;
        this.sortBy = sortBy;
    }

    public void triggerSearch(Context context) {
        if (EvaComponent.evaAppHandler != null && EvaComponent.evaAppHandler instanceof FlightSearch) {

        }
    }
}
