package com.evature.evasdk.model.appmodel;

import android.content.Context;

import com.evature.evasdk.appinterface.FlightSearch;
import com.evature.evasdk.appinterface.HotelSearch;
import com.evature.evasdk.evaapis.EvaComponent;
import com.evature.evasdk.evaapis.crossplatform.EvaLocation;
import com.evature.evasdk.evaapis.crossplatform.EvaTravelers;
import com.evature.evasdk.evaapis.crossplatform.FlightAttributes;
import com.evature.evasdk.evaapis.crossplatform.HotelAttributes;
import com.evature.evasdk.evaapis.crossplatform.RequestAttributes;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;

/**
 * Created by iftah on 6/2/15.
 */
public class AppHotelSearchModel extends AppSearchModel {

    private final EvaLocation location;

    private final Date arriveDateMin;
    private final Date arriveDateMax;
    private final Integer durationMin;
    private final Integer durationMax;
    private final EvaTravelers travelers;
    private final ArrayList<HotelAttributes.HotelChain> chains;

    // The hotel board:
    private final Boolean selfCatering;
    private final Boolean bedAndBreakfast;
    private final Boolean halfBoard;
    private final Boolean fullBoard;
    private final Boolean allInclusive;
    private final Boolean drinksInclusive;

    // The quality of the hotel, measure in Stars
    private final Integer minStars;
    private final Integer maxStars;

    private final HashSet<HotelAttributes.Amenities> amenities;

    private final RequestAttributes.SortEnum sortBy;
    private final RequestAttributes.SortOrderEnum sortOrder;

    /***
     *
     * @param isComplete
     * @param location
     * @param travelers
     * @param sortBy
     * @param sortOrder
     */
    public AppHotelSearchModel(boolean isComplete, EvaLocation location,
                               Date arriveDateMin, Date arriveDateMax,
                               Integer durationMin, Integer durationMax,
                               EvaTravelers travelers,
                               ArrayList<HotelAttributes.HotelChain> chains,

                               // The hotel board:
                               Boolean selfCatering,
                               Boolean bedAndBreakfast,
                               Boolean halfBoard,
                               Boolean fullBoard,
                               Boolean allInclusive,
                               Boolean drinksInclusive,

                               // The quality of the hotel, measure in Stars
                               Integer minStars,
                               Integer maxStars,

                               HashSet<HotelAttributes.Amenities> amenities,
                               RequestAttributes.SortEnum sortBy,
                               RequestAttributes.SortOrderEnum sortOrder) {
        super(isComplete);
        this.location = location;
        this.travelers = travelers;
        this.chains = chains;
        this.arriveDateMax = arriveDateMax;
        this.arriveDateMin = arriveDateMin;
        this.durationMax = durationMax;
        this.durationMin = durationMin;
        this.selfCatering = selfCatering;
        this.bedAndBreakfast = bedAndBreakfast;
        this.halfBoard = halfBoard;
        this.fullBoard = fullBoard;
        this.allInclusive = allInclusive;
        this.drinksInclusive = drinksInclusive;
        this.minStars = minStars;
        this.maxStars = maxStars;
        this.amenities = amenities;
        this.sortBy = sortBy;
        this.sortOrder = sortOrder;
    }

    public void triggerSearch(Context context) {
        if (EvaComponent.evaAppHandler instanceof HotelSearch) {
            ((HotelSearch) EvaComponent.evaAppHandler).handleHotelSearch(context, isComplete,
                    location,
                    arriveDateMin, arriveDateMax,
                    durationMin, durationMax,
                    travelers,
                    chains,

                    // The hotel board:
                    selfCatering,
                    bedAndBreakfast,
                    halfBoard,
                    fullBoard,
                    allInclusive,
                    drinksInclusive,

                    // The quality of the hotel, measure in Stars
                    minStars,
                    maxStars,

                    amenities,
                    sortBy, sortOrder);
        }
    }
}
