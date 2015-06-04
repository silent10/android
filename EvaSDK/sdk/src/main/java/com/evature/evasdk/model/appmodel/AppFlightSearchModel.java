package com.evature.evasdk.model.appmodel;

import android.content.Context;

import com.evature.evasdk.appinterface.FlightSearch;
import com.evature.evasdk.evaapis.android.EvaComponent;
import com.evature.evasdk.evaapis.crossplatform.EvaLocation;
import com.evature.evasdk.evaapis.crossplatform.EvaTravelers;
import com.evature.evasdk.evaapis.crossplatform.FlightAttributes;
import com.evature.evasdk.evaapis.crossplatform.RequestAttributes;

import java.util.Date;

/**
 * Created by iftah on 6/2/15.
 */
public class AppFlightSearchModel extends AppSearchModel {

    private final EvaLocation origin;
    private final EvaLocation destination;
    private final RequestAttributes.SortEnum sortBy;

    private final Date departDateMin;
    private final Date departDateMax;
    private final Date returnDateMin;
    private final Date returnDateMax;
    private final EvaTravelers travelers;

    private final Boolean nonstop; // A Non stop flight - Boolean attribute; null= not specified, false = explicitly request NOT nonstop, true = explicitly requested nonstop flight
    private final Boolean redeye; // A Red eye flight - Boolean attribute; null= not specified, false = explicitly request NOT red eye, true = explicitly requested red eye flight
    private final boolean oneWay;
    private final String[] airlines;
    private final String food;
    private final FlightAttributes.SeatType seatType;
    private final FlightAttributes.SeatClass[] seatClass;

    /***
     *
     * @param isComplete
     * @param origin
     * @param destination
     * @param departDateMin
     * @param departDateMax
     * @param returnDateMin
     * @param returnDateMax
     * @param travelers
     * @param nonstop - A Non stop flight - Boolean attribute; null= not specified, false = explicitly request NOT nonstop, true = explicitly requested nonstop flight
     * @param redeye - A Red eye flight - Boolean attribute; null= not specified, false = explicitly request NOT red eye, true = explicitly requested red eye flight
     * @param oneWay
     * @param airlines
     * @param food
     * @param seatType
     * @param seatClass
     * @param sortBy
     */
    public AppFlightSearchModel(boolean isComplete, EvaLocation origin, EvaLocation destination,
                                Date departDateMin, Date departDateMax,
                                Date returnDateMin, Date returnDateMax,
                                EvaTravelers travelers,
                                boolean oneWay,
                                Boolean nonstop,
                                FlightAttributes.SeatClass[] seatClass,
                                String[] airlines,
                                Boolean redeye,
                                String food,
                                FlightAttributes.SeatType seatType,
                                RequestAttributes.SortEnum sortBy) {
        super(isComplete);
        this.origin = origin;
        this.destination = destination;
        this.sortBy = sortBy;
        this.departDateMin = departDateMin;
        this.departDateMax = departDateMax;
        this.returnDateMin = returnDateMin;
        this.returnDateMax = returnDateMax;
        this.travelers = travelers;
        this.nonstop = nonstop;
        this.redeye = redeye;
        this.oneWay = oneWay;
        this.airlines = airlines;
        this.food = food;
        this.seatType = seatType;
        this.seatClass = seatClass;
    }

    public void triggerSearch(Context context) {
        if (EvaComponent.evaAppHandler instanceof FlightSearch) {
            if (oneWay) {
                ((FlightSearch) EvaComponent.evaAppHandler).handleOneWayFlightSearch(context,  isComplete,  origin,  destination,
                         departDateMin,  departDateMax,
                         returnDateMin,  returnDateMax,
                         travelers,
                 nonstop, seatClass,airlines,
                 redeye,
                 food, seatType,sortBy);
            }
            else {
                ((FlightSearch) EvaComponent.evaAppHandler).handleRoundTripFlightSearch(context,  isComplete,  origin,  destination,
                        departDateMin,  departDateMax,
                        returnDateMin,  returnDateMax,
                        travelers,
                        nonstop, seatClass,airlines,
                        redeye,
                        food, seatType,sortBy);
            }
        }
    }
}
