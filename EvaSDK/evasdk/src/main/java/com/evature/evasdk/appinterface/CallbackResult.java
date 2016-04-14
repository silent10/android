package com.evature.evasdk.appinterface;

import android.text.Spannable;
import android.text.SpannableString;

import java.util.concurrent.Future;

/**
 * Created by iftah on 07/04/2016.
 */
public class CallbackResult {
    private String sayIt;
    private Spannable displayIt;
    private Future<CallbackResult> deferredResult;
    private boolean appendToExistingText; // append the display/say strings to the Eva reply
    private boolean closeChat;  // set to true to close the chat screen immediately after the result handling is complete
    private int countResults;   // special handling for callbacks that trigger search - return the number of results
                                // Eva will alter the default reply if the results is 0  ("no such elements found")
                                // or if the results is 1
    private Future<Integer> deferredCountResults;

    private CallbackResult(String sayIt, Spannable displayIt, Future<CallbackResult> deferredResult) {
        this.sayIt = sayIt;
        this.displayIt = displayIt;
        this.deferredResult = deferredResult;
    }

    private CallbackResult(String sayIt, Spannable displayIt) {
        this(sayIt, displayIt, null);
    }

    // default handling - say+display Eva's text  - same as returning null from the callback
    public static CallbackResult defaultHandling() {
        return new CallbackResult(null, null);
    }

    // do nothing (no say nor display)
    public static CallbackResult doNothing() {
        return new CallbackResult("", new SpannableString(""));
    }

    // display+say the same string
    public static CallbackResult textResult(String text) {
        return new CallbackResult(text, new SpannableString(text));
    }

    public static CallbackResult textResult(Spannable text) {
        return new CallbackResult(text.toString(), text);
    }

    // display one string and say another
    // null = default text,    "" = do nothing
    public static CallbackResult textResult(String sayIt, Spannable displayIt) {
        return new CallbackResult(sayIt, displayIt);
    }
    public static CallbackResult textResult(String sayIt, String displayIt) {
        return new CallbackResult(sayIt, new SpannableString(displayIt));
    }

    // the future will resolve to a EVCallbackResult, nothing will be spoken/displayed until then
    public static CallbackResult delayedResult(Future<CallbackResult> future) {
        return new CallbackResult("", new SpannableString(""), future);
    }

    // handle the immediate result (eg. say/display) and then replace it with the result which will be resolved by the promise
    public static CallbackResult delayedResult(CallbackResult immediateResult, Future<CallbackResult> futureResult) {
        return new CallbackResult(immediateResult.sayIt, immediateResult.displayIt, futureResult);
    }

    public static CallbackResult countResult(int count) {
        CallbackResult result = CallbackResult.doNothing();
        result.countResults = count;
        return result;
    }
    public static CallbackResult countResult(Future<Integer> deferredCount) {
        CallbackResult result = CallbackResult.doNothing();
        result.deferredCountResults = deferredCount;
        return result;
    }

    public boolean isAppendToExistingText() {
        return appendToExistingText;
    }

    public void setAppendToExistingText(boolean appendToExistingText) {
        this.appendToExistingText = appendToExistingText;
    }

    public boolean isCloseChat() {
        return closeChat;
    }

    public void setCloseChat(boolean closeChat) {
        this.closeChat = closeChat;
    }

    public int getCountResult() {
        return countResults;
    }

    public void setCountResult(int countResult) {
        this.countResults = countResult;
    }

    public String getSayIt() {
        return sayIt;
    }

    public Spannable getDisplayIt() {
        return displayIt;
    }

    public Future<CallbackResult> getDeferredResult() {
        return deferredResult;
    }


}
