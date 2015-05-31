<#include "generated_file_warning.txt">

package com.evature.evasdk;

import java.io.Serializable;
import java.util.Date;
import android.content.Context;

import com.evature.evasdk.evaapis.crossplatform.CruiseAttributes;
import com.evature.evasdk.evaapis.crossplatform.RequestAttributes;
import com.evature.evasdk.evaapis.crossplatform.EvaLocation;


public class EvaAppSearchModel  implements Serializable {

    public EvaLocation from;
    public EvaLocation to;
    public Date dateFrom;
    public Date dateTo;
    public Integer durationMin;
    public Integer durationMax;
    public CruiseAttributes attributes;
    public RequestAttributes.SortEnum sortBy;


    public EvaAppSearchModel(EvaLocation from,
                             EvaLocation to,
                             Date dateFrom,
                             Date dateTo,
                             Integer durationMin,
                             Integer durationMax,
                             CruiseAttributes attributes,
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
        if (EvaAppSetup.evaAppHandler != null) {
            EvaAppSetup.evaAppHandler.handleCruiseSearch(context, from, to, dateFrom, dateTo, durationMin, durationMax, attributes, sortBy);
        }
    }
}
