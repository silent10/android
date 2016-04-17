package com.evature.evasdk.appinterface;

import android.text.Spannable;
import android.text.SpannableString;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

/**
 * Created by iftah on 07/04/2016.
 */
public class CallbackResult {
    private static final Executor executor = Executors.newCachedThreadPool();
    private String sayIt;
    private Spannable displayIt;
    private Future<CallbackResult> deferredResult;
    private boolean appendToExistingText; // append the display/say strings to the Eva reply
    private boolean closeChat;      // set to true to close the chat screen immediately after the result handling is complete
    private int countResults = -1;  // special handling for callbacks that trigger search - return the number of results
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
    public static CallbackResult delayedResult(FutureTask<CallbackResult> future) {
        executor.execute(future);
        return new CallbackResult("", new SpannableString(""), future);
    }

    // handle the immediate result (eg. say/display) and then replace it with the result which will be resolved by the promise
    // side affect: executes the futureResult in a thread
    public static CallbackResult delayedResult(CallbackResult immediateResult, FutureTask<CallbackResult> futureResult) {
        executor.execute(futureResult);
        return new CallbackResult(immediateResult.sayIt, immediateResult.displayIt, futureResult);
    }

    public static CallbackResult countResult(int count) {
        CallbackResult result = CallbackResult.defaultHandling();
        result.countResults = count;
        return result;
    }

    // side affect: executes the deferredCount in a thread
    public static CallbackResult countResult(FutureTask<Integer> deferredCount) {
        CallbackResult result = CallbackResult.defaultHandling();
        executor.execute(deferredCount);
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


    public Future<Integer> getDeferredCountResults() {
        return deferredCountResults;
    }

    public void setDeferredCountResults(Future<Integer> deferredCountResults) {
        this.deferredCountResults = deferredCountResults;
    }
}
