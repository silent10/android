package com.evature.evasdk.appinterface;

/**
 * Created by iftah on 6/2/15.
 */
public interface AsyncCountResult {
    /***
     * When the count of a query (eg. how many search results for flights from NY to LA on monday) is returned, this method is activated with the result
     * @param count
     */
    void handleCountResult(int count);
}
