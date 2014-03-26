/*
 * Copyright (c) 2013, Expedia Affiliate Network
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that redistributions of source code
 * retain the above copyright notice, these conditions, and the following
 * disclaimer. 
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are those
 * of the authors and should not be interpreted as representing official policies, 
 * either expressed or implied, of the Expedia Affiliate Network or Expedia Inc.
 */

package com.ean.mobile.exception;

import org.json.JSONObject;

/**
 * A wrapper for the EanWsError object that can come back in the case of a failure in the request.
 */
public class EanWsError extends Exception {

    /**
     * The category of error. Often "RECOVERABLE".
     */
    public final String category;

    /**
     * The localized, plain-speak version of the error's main message.
     */
    public final String presentationMessage;

	public boolean recoverable;

    /**
     * The main constructor. The verbose message is used as the exception's main message.
     * @param verboseMessage The message for the exception.
     * @param category The "category" of the error.
     * @param presentationMessage The localized, simplified version of verboseMessage.
     */
    public EanWsError(final String verboseMessage, final String category, final String presentationMessage, final boolean recoverable) {
        super(verboseMessage);
        this.category = category;
        this.presentationMessage = presentationMessage;
        this.recoverable = recoverable;
    }

    /**
     * Gets an EanWsError object from a representative JSONObject.
     * @param error The error that has occurred in JSON.
     * @return The java exception that the error's object implies.
     */
    public static EanWsError fromJson(final JSONObject error) {
        if (error.has("category") && "DATA_VALIDATION".equals(error.optString("category"))) {
            return new DataValidationException(
                error.optString("verboseMessage"),
                error.optString("category"),
                error.optString("presentationMessage"));
        }
        return new EanWsError(
            error.optString("verboseMessage"),
            error.optString("category"),
            error.optString("presentationMessage"),
            error.optString("handling").equals("RECOVERABLE"));
    }
}
