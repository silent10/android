package com.evature.evasdk.appinterface;

import android.content.Context;

import com.evature.evasdk.evaapis.crossplatform.EvaLocation;
import com.evature.evasdk.evaapis.crossplatform.EvaTravelers;
import com.evature.evasdk.evaapis.crossplatform.HotelAttributes;
import com.evature.evasdk.evaapis.crossplatform.RequestAttributes.SortEnum;
import com.evature.evasdk.evaapis.crossplatform.RequestAttributes.SortOrderEnum;

import java.util.Date;

/**
 * Created by iftah on 6/2/15.
 */
public interface EvaHotelSearch {

    EvaResult handleHotelSearch(Context context,
                                boolean isComplete, EvaLocation location,
                                Date arriveDateMin, Date arriveDateMax,
                                Integer durationMin, Integer durationMax,
                                EvaTravelers travelers,
                                HotelAttributes attributes,
                                SortEnum sortBy, SortOrderEnum sortOrder);

}
