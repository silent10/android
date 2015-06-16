package com.evature.evasdk.appinterface;

import android.content.Context;

import com.evature.evasdk.evaapis.crossplatform.EvaLocation;
import com.evature.evasdk.evaapis.crossplatform.EvaTravelers;
import com.evature.evasdk.evaapis.crossplatform.FlightAttributes.SeatClass;
import com.evature.evasdk.evaapis.crossplatform.FlightAttributes.SeatType;
import com.evature.evasdk.evaapis.crossplatform.RequestAttributes.SortOrderEnum;
import com.evature.evasdk.evaapis.crossplatform.RequestAttributes.SortEnum;

import java.util.Date;

/**
 * Created by iftah on 6/2/15.
 */
public interface FlightSearch {


    void handleOneWayFlightSearch(Context context,
                                  boolean isComplete, EvaLocation origin, EvaLocation destination,
                                  Date departDateMin, Date departDateMax,
                                  Date returnDateMin, Date returnDateMax,
                                  EvaTravelers travelers,
                                  Boolean nonstop,
                                  SeatClass[] seatClass,
                                  String[] airlines,
                                  Boolean redeye,
                                  String food,
                                  SeatType seatType,
                                  SortEnum sortBy, SortOrderEnum sortOrder);

    void handleRoundTripFlightSearch(Context context,
                                     boolean isComplete, EvaLocation origin, EvaLocation destination,
                                     Date departDateMin, Date departDateMax,
                                     Date returnDateMin, Date returnDateMax,
                                     EvaTravelers travelers,
                                     Boolean nonstop,
                                     SeatClass[] seatClass,
                                     String[] airlines,
                                     Boolean redeye,
                                     String food,
                                     SeatType seatType,
                                     SortEnum sortBy, SortOrderEnum sortOrder);
}
