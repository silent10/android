package com.evature.evasdk.model.appmodel;

import android.content.Context;

import com.evature.evasdk.appinterface.CruiseSearch;
import com.evature.evasdk.evaapis.android.EvaComponent;
import com.evature.evasdk.evaapis.crossplatform.CruiseAttributes;
import com.evature.evasdk.evaapis.crossplatform.EvaLocation;
import com.evature.evasdk.evaapis.crossplatform.RequestAttributes;

import java.util.Date;

/**
 * Created by iftah on 6/2/15.
 */
public class AppCruiseSearchModel extends AppSearchModel {
    private final EvaLocation from;
    private final EvaLocation to;
    private final Date dateFrom;
    private final Date dateTo;
    private final Integer durationMin;
    private final Integer durationMax;
    private final CruiseAttributes attributes;
    private final RequestAttributes.SortEnum sortBy;


    public AppCruiseSearchModel(boolean isComplete,
                                EvaLocation from,
                                EvaLocation to,
                                Date dateFrom,
                                Date dateTo,
                                Integer durationMin,
                                Integer durationMax,
                                CruiseAttributes attributes,
                                RequestAttributes.SortEnum sortBy) {
        super(isComplete);
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
        if (EvaComponent.evaAppHandler instanceof CruiseSearch) {
            ((CruiseSearch)EvaComponent.evaAppHandler).handleCruiseSearch(context, isComplete, from, to, dateFrom, dateTo, durationMin, durationMax, attributes, sortBy);
        }
    }

}
