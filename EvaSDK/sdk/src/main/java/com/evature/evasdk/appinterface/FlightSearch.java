package com.evature.evasdk.appinterface;

import android.content.Context;

import com.evature.evasdk.evaapis.crossplatform.CruiseAttributes;
import com.evature.evasdk.evaapis.crossplatform.EvaLocation;
import com.evature.evasdk.evaapis.crossplatform.FlightAttributes;
import com.evature.evasdk.evaapis.crossplatform.RequestAttributes;

import java.util.Date;

/**
 * Created by iftah on 6/2/15.
 */
public interface FlightSearch {


    void handleFlightSearch(Context context,
                            boolean isComplete,
                            EvaLocation from, EvaLocation to,
                            Date dateFrom, Date dateTo,
                            Integer durationMin, Integer durationMax,
                            FlightAttributes attributes,
                            RequestAttributes.SortEnum sortBy);
}
