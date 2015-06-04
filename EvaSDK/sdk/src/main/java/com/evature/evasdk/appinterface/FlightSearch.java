package com.evature.evasdk.appinterface;

import android.content.Context;

import com.evature.evasdk.evaapis.crossplatform.CruiseAttributes;
import com.evature.evasdk.evaapis.crossplatform.EvaLocation;
import com.evature.evasdk.evaapis.crossplatform.EvaTravelers;
import com.evature.evasdk.evaapis.crossplatform.FlightAttributes;
import com.evature.evasdk.evaapis.crossplatform.RequestAttributes;

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
                                  FlightAttributes.SeatClass[] seatClass,
                                  String[] airlines,
                                  Boolean redeye,
                                  String food,
                                  FlightAttributes.SeatType seatType,
                                  RequestAttributes.SortEnum sortBy);

    void handleRoundTripFlightSearch(Context context,
                                     boolean isComplete, EvaLocation origin, EvaLocation destination,
                                     Date departDateMin, Date departDateMax,
                                     Date returnDateMin, Date returnDateMax,
                                     EvaTravelers travelers,
                                     Boolean nonstop,
                                     FlightAttributes.SeatClass[] seatClass,
                                     String[] airlines,
                                     Boolean redeye,
                                     String food,
                                     FlightAttributes.SeatType seatType,
                                     RequestAttributes.SortEnum sortBy);
}
