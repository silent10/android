package com.evature.evasdk.appinterface;

import android.content.Context;

import com.evature.evasdk.evaapis.crossplatform.CruiseAttributes;
import com.evature.evasdk.evaapis.crossplatform.EvaLocation;

import java.util.Date;

/**
 * Created by iftah on 6/2/15.
 */
public interface CruiseCount {

    void getCruiseCount(Context context,
                        EvaLocation from, EvaLocation to,
                        Date dateFrom, Date dateTo,
                        Integer durationMin, Integer durationMax,
                        CruiseAttributes attributes,
                        AsyncCountResult callback);
}
