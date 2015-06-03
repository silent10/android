package com.evature.evasdk.appinterface;

/**
 * Created by iftah on 6/3/15.
 */
public enum AppScope {

    Flight,
    Hotel,
    Car,
    Cruise,
    Vacation,
    Ski,
    Explore;

    @Override
    public String toString() {
        switch(this) {
            case Flight: return "f";
            case Hotel: return "h";
            case Car: return "c";
            case Cruise: return "r";
            case Vacation: return "v";
            case Ski: return "s";
            case Explore: return "e";
            default: throw new IllegalArgumentException();
        }
    }
}
