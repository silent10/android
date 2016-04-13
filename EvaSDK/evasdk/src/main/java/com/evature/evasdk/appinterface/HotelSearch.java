package com.evature.evasdk.appinterface;

import android.content.Context;

import com.evature.evasdk.evaapis.crossplatform.EvaLocation;
import com.evature.evasdk.evaapis.crossplatform.EvaTravelers;
import com.evature.evasdk.evaapis.crossplatform.HotelAttributes;
import com.evature.evasdk.evaapis.crossplatform.RequestAttributes.SortEnum;
import com.evature.evasdk.evaapis.crossplatform.RequestAttributes.SortOrderEnum;
import com.evature.evasdk.evaapis.crossplatform.HotelAttributes.Amenities;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;

/**
 * Created by iftah on 6/2/15.
 */
public interface HotelSearch {

    CallbackResult handleHotelSearch(Context context,
                                     boolean isComplete, EvaLocation location,
                                     Date arriveDateMin, Date arriveDateMax,
                                     Integer durationMin, Integer durationMax,
                                     EvaTravelers travelers,
                                     HotelAttributes attributes,
                                     SortEnum sortBy, SortOrderEnum sortOrder);

}
