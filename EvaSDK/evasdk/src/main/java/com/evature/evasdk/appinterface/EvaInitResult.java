package com.evature.evasdk.appinterface;

import java.util.ArrayList;

/**
 * Created by iftah on 6/2/15.
 */
public interface EvaInitResult {

    enum InitResultEnum {
        OK,         // no problem detected
        Warning,    // there was a problem - but it might be temporary (eg. no network)
        Error       // there was a fatal error (eg. bad credentials)
    }
    /****
     *
     * @param result
     * @param msg - String description of error/warning if there was any, or null
     * @param e - root cause exception
     */
    void initResult(InitResultEnum result, String msg, Exception e);


}
