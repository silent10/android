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

package com.ean.mobile.request;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.util.Log;

import com.ean.mobile.Constants;
import com.ean.mobile.exception.CommonParameterValidationException;

/**
 * Contains elements that (1) are common to all requests and (2) typically remain the same across multiple requests.
 */
public final class CommonParameters {

    /**
     * The CID to use for API requests. Required for all API calls.
     */
    public static volatile String cid;

    /**
     * The API key to use for API requests. Required for all API calls.
     */
    public static volatile String apiKey;
    
    /**
     * This is signature used to calculate signatures for non-ip authentication.
     */
    public static volatile String signatureSecret;

    /**
     * The user agent to use for API requests.
     */
    public static volatile String customerUserAgent;

    /**
     * The locale to use for API requests.
     */
    public static volatile String locale;

    /**
     * The currency code to use for API requests.
     */
    public static volatile String currencyCode;

    /**
     * The customer IP address to use for API requests.
     */
    public static volatile String customerIpAddress;

    /**
     * The session ID to use for API requests. Set automatically when returned as part of a hotel list response.
     */
    public static volatile String customerSessionId;

    /**
     * The minor revision to use for API requests. The library has only been tested with the default minor revision,
     * so this should only be changed if absolutely necessary!
     */
    public static volatile String minorRev = Constants.MINOR_REV;

    private static final int MILLISECONDS_PER_SECOND = 1000;

    /**
     * Private, no-op constructor to prevent instantiation.
     */
    private CommonParameters() {

    }

    /**
     * Convenience method that returns all set parameters as a list of NameValuePair objects for use in API requests.
     *
     * @return a list of NameValuePair objects containing each attribute name and value.
     */
    public static List<NameValuePair> asNameValuePairs() {
        validateParameters();

        final List<NameValuePair> nameValuePairs = new LinkedList<NameValuePair>();

        nameValuePairs.add(new BasicNameValuePair("cid", cid));
        nameValuePairs.add(new BasicNameValuePair("apiKey", apiKey));

        if (signatureSecret != null) {
            nameValuePairs.add(new BasicNameValuePair("sig", getSignature()));
        }
        if (customerUserAgent != null) {
            nameValuePairs.add(new BasicNameValuePair("customerUserAgent", customerUserAgent));
        }
        if (locale != null) {
            nameValuePairs.add(new BasicNameValuePair("locale", locale));
        }
        if (currencyCode != null) {
            nameValuePairs.add(new BasicNameValuePair("currencyCode", currencyCode));
        }
        if (customerIpAddress != null) {
            nameValuePairs.add(new BasicNameValuePair("customerIpAddress", customerIpAddress));
        }
        if (customerSessionId == null || "".equals(customerSessionId)) {
        	customerSessionId = UUID.randomUUID().toString();
        }
        nameValuePairs.add(new BasicNameValuePair("customerSessionId", customerSessionId));
        if (minorRev != null) {
            nameValuePairs.add(new BasicNameValuePair("minorRev", minorRev));
        }

        return nameValuePairs;
    }

    /**
     * Checks that all variables necessary to execute a request have been initialized. If not,
     * a CommonParameterValidationException is thrown.
     */
    private static void validateParameters() {
        if (cid == null || apiKey == null) {
            throw new CommonParameterValidationException(
                "You MUST initialize both the cid and apiKey in CommonParameters before performing any requests!");
        }
    }

    /**
     * This generates a signature if a secret is set, otherwise it returns null.
     * @return Calculated signature.
     */
    private static String getSignature() {
        if (signatureSecret != null) {
            try {
                final MessageDigest messageDigest = MessageDigest.getInstance("MD5");
                final long timeInSeconds = System.currentTimeMillis() / MILLISECONDS_PER_SECOND;
                final String signatureInput = apiKey + signatureSecret + timeInSeconds;

                messageDigest.update(signatureInput.getBytes());
                return String.format("%032x", new BigInteger(1, messageDigest.digest()));
            } catch (NoSuchAlgorithmException e) {
                Log.e(Constants.LOG_TAG, "Couldn't get MD5 hashing working.", e);
            }
        }
        return null;
    }

}
