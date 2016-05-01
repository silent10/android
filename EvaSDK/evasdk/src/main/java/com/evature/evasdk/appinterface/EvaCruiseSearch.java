package com.evature.evasdk.appinterface;

import android.content.Context;

import com.evature.evasdk.evaapis.crossplatform.CruiseAttributes;
import com.evature.evasdk.evaapis.crossplatform.EvaLocation;
import com.evature.evasdk.evaapis.crossplatform.RequestAttributes.SortOrderEnum;
import com.evature.evasdk.evaapis.crossplatform.RequestAttributes.SortEnum;

import java.util.Date;

/**
 * Created by iftah on 6/2/15.
 */
public interface EvaCruiseSearch {

    /***
     *
     * @param context
     * @param isComplete - if true then all the mandatory fields are not null
     * @param from
     * @param to
     * @param dateFrom
     * @param dateTo
     * @param durationMin
     * @param durationMax
     * @param attributes
     * @param sortBy
     */
    EvaResult handleCruiseSearch(Context context,
                                 boolean isComplete,
                                 EvaLocation from, EvaLocation to,
                                 Date dateFrom, Date dateTo,
                                 Integer durationMin, Integer durationMax,
                                 CruiseAttributes attributes,
                                 SortEnum sortBy, SortOrderEnum sortOrder);
}
