package com.evature.evasdk.appinterface;

import android.content.Context;

import com.evature.evasdk.evaapis.crossplatform.EvaLocation;
import com.evature.evasdk.evaapis.crossplatform.EvaTravelers;
import com.evature.evasdk.evaapis.crossplatform.FlightAttributes;
import com.evature.evasdk.evaapis.crossplatform.RequestAttributes;

import java.util.Date;

/**
 * Created by iftah on 6/2/15.
 */
public interface FlightCount {

    void getOneWayFlightCount(Context context,
                              boolean isComplete,
                              EvaLocation origin, EvaLocation destination,
                              Date departDateMin, Date departDateMax,
                              Date returnDateMin, Date returnDateMax,
                              EvaTravelers travelers,
                              Boolean nonstop,
                              FlightAttributes.SeatClass[] seatClass,
                              String[] airlines,
                              Boolean redeye,
                              String food,
                              FlightAttributes.SeatType seatType,
                              AsyncCountResult callback);

    void getRoundTripFlightCount(Context context,
                                 boolean isComplete,
                                 EvaLocation origin, EvaLocation destination,
                                 Date departDateMin, Date departDateMax,
                                 Date returnDateMin, Date returnDateMax,
                                 EvaTravelers travelers,
                                 Boolean nonstop,
                                 FlightAttributes.SeatClass[] seatClass,
                                 String[] airlines,
                                 Boolean redeye,
                                 String food,
                                 FlightAttributes.SeatType seatType,
                                 AsyncCountResult callback
                                 );
}
