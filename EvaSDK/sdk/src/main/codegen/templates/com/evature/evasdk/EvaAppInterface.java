<#include "generated_file_warning.txt" >

package com.evature.evasdk;

import com.evature.evasdk.evaapis.crossplatform.EvaLocation;


public interface EvaAppInterface {

<#if scope.flight>

    /****
     * Trigger a flight search - opens a new activity with results
     * @param from
     * @param to
     */
    void handleFlightSearch(EvaLocation from,  EvaLocation to);

    <#if (features.getCount)?? >

    /****
     *
     * @param from
     * @param to
     * @return  number of matching flights
     */
    int getFlightCount(EvaLocation from,  EvaLocation to);
    </#if>
</#if>
<#if scope.cruise>

    /****
     *
     * @param from
     * @param to
     */
    void handleCruiseSearch(EvaLocation from,  EvaLocation to);
    <#if (features.getCount)?? >

    /****
     *
     * @param from
     * @param to
     * @return number of matching cruises
     */
    int getCruiseCount(EvaLocation from,  EvaLocation to);
    </#if>
</#if>

}