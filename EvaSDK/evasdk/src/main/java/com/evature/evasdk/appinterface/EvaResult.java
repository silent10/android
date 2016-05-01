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
public class EvaResult {
    private static final Executor executor = Executors.newCachedThreadPool();
    private String sayIt;
    private Spannable displayIt;
    private Future<EvaResult> deferredResult;
    private boolean appendToExistingText; // append the display/say strings to the Eva reply
    private boolean closeChat;      // set to true to close the chat screen immediately after the result handling is complete
    private int countResults = -1;  // special handling for callbacks that trigger search - return the number of results
                                    // Eva will alter the default reply if the results is 0  ("no such elements found")
                                    // or if the results is 1
    private Future<Integer> deferredCountResults;

    private EvaResult(String sayIt, Spannable displayIt, Future<EvaResult> deferredResult) {
        this.sayIt = sayIt;
        this.displayIt = displayIt;
        this.deferredResult = deferredResult;
    }

    private EvaResult(String sayIt, Spannable displayIt) {
        this(sayIt, displayIt, null);
    }

    // default handling - say+display Eva's text  - same as returning null from the callback
    public static EvaResult defaultHandling() {
        return new EvaResult(null, null);
    }
    // default handling - say+display Eva's text  - same as returning null from the callback
    public static EvaResult defaultHandling(boolean closeChat) {
        EvaResult result = new EvaResult(null, null);
        result.setCloseChat(closeChat);
        return result;
    }

    // do nothing (no say nor display)
    public static EvaResult doNothing() {
        return new EvaResult("", new SpannableString(""));
    }

    // display+say the same string
    public static EvaResult textResult(String text) {
        return new EvaResult(text, new SpannableString(text));
    }

    public static EvaResult textResult(Spannable text) {
        return new EvaResult(text.toString(), text);
    }

    // display one string and say another
    // null = default text,    "" = do nothing
    public static EvaResult textResult(String sayIt, Spannable displayIt) {
        return new EvaResult(sayIt, displayIt);
    }
    public static EvaResult textResult(String sayIt, String displayIt) {
        return new EvaResult(sayIt, new SpannableString(displayIt));
    }

    // the future will resolve to a EVCallbackResult, nothing will be spoken/displayed until then
    public static EvaResult delayedResult(FutureTask<EvaResult> future) {
        executor.execute(future);
        return new EvaResult("", new SpannableString(""), future);
    }

    // handle the immediate result (eg. say/display) and then replace it with the result which will be resolved by the promise
    // side affect: executes the futureResult in a thread
    public static EvaResult delayedResult(EvaResult immediateResult, FutureTask<EvaResult> futureResult) {
        executor.execute(futureResult);
        return new EvaResult(immediateResult.sayIt, immediateResult.displayIt, futureResult);
    }

    public static EvaResult countResult(int count) {
        EvaResult result = EvaResult.defaultHandling();
        result.countResults = count;
        return result;
    }

    // side affect: executes the deferredCount in a thread
    public static EvaResult countResult(FutureTask<Integer> deferredCount) {
        EvaResult result = EvaResult.defaultHandling();
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

    public Future<EvaResult> getDeferredResult() {
        return deferredResult;
    }


    public Future<Integer> getDeferredCountResults() {
        return deferredCountResults;
    }

    public void setDeferredCountResults(Future<Integer> deferredCountResults) {
        this.deferredCountResults = deferredCountResults;
    }
}
