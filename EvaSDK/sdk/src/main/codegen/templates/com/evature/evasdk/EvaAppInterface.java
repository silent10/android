<#include "generated_file_warning.txt" >

package com.evature.evasdk;

import android.content.Context;

<#if scope.cruise>
import com.evature.evasdk.evaapis.crossplatform.CruiseAttributes;
</#if>
import com.evature.evasdk.evaapis.crossplatform.EvaLocation;
import com.evature.evasdk.evaapis.crossplatform.RequestAttributes;

import java.util.Date;


public interface EvaAppInterface {

<#if async_count >
    interface AsyncCountResult {
        /***
         * When the count of a query (eg. how many search results for flights from NY to LA on monday) is returned, this method is activated with the result
         * @param count
         */
        void handleCountResult(int count);
    }
</#if>

<#if scope.flight>

    /****
     * Trigger a flight search - opens a new activity with results
     * @param from
     * @param to
     */
    void handleFlightSearch(Context context, EvaLocation from,  EvaLocation to);

    <#if (async_count)?? >
    /****
     *
     * @param from
     * @param to
     *
     */
    void getFlightCount(Context context, EvaLocation from,  EvaLocation to);
    </#if>
</#if>
<#if scope.cruise>

    /****
     *
     * @param from
     * @param to
     */
    void handleCruiseSearch(Context context,
                            EvaLocation from, EvaLocation to,
                            Date dateFrom, Date dateTo,
                            Integer durationMin, Integer durationMax,
                            CruiseAttributes attributes,
                            RequestAttributes.SortEnum sortBy);
    <#if (async_count)?? >

    /****
     *
     * @param from
     * @param to
     */
    void getCruiseCount(Context context,
                        EvaLocation from, EvaLocation to,
                        Date dateFrom, Date dateTo,
                        Integer durationMin, Integer durationMax,
                        CruiseAttributes attributes,
                        AsyncCountResult callback);
    </#if>
</#if>

}