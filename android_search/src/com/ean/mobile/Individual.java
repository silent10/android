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

package com.ean.mobile;

import java.util.Arrays;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;


/**
 * Data holder for information about a particular individual.
 * DO NOT SERIALIZE OR SAVE ANYWHERE.
 */
public abstract class Individual {


    /**
     * The email of the individual.
     */
    public final String email;

    /**
     * The name of the individual.
     */
    public final Name name;

    /**
     * The individual's home telephone number.
     */
    public final String homePhone;

    /**
     * The individual's work telephone number.
     */
    public final String workPhone;

    /**
     * The constructor for the holder for information about a particular individual.
     * @param email The individual's email.
     * @param firstName The individual's first name.
     * @param lastName The individual's last name.
     * @param homePhone The individual's home telephone number.
     * @param workPhone The individual's work telephone number.
     */
    public Individual(final String email, final String firstName, final String lastName,
            final String homePhone, final String workPhone) {
        this.email = email;
        this.name = new Name(firstName, lastName);
        this.homePhone = homePhone;
        this.workPhone = workPhone;
    }

    /**
     * Constructs an individual from a JSONObject who has email, firstName, lastName, homePhone, and workPhone
     * fields.
     * @param object The JSONObject that contains the aforementioned fields.
     */
    public Individual(final JSONObject object) {
        this.email = object.optString("email");
        this.name = new Name(object);
        this.homePhone = object.optString("homePhone");
        this.workPhone = object.optString("workPhone");
    }

    /**
     * Gets NameValuePairs for the reservation information so it can be sent in a rest request.
     * @return The requested NameValuePairs
     */
    public List<NameValuePair> asNameValuePairs() {
        return Arrays.<NameValuePair>asList(
            new BasicNameValuePair("email", email),
            new BasicNameValuePair("firstName", name.first),
            new BasicNameValuePair("lastName", name.last),
            homePhone == null ? null : new BasicNameValuePair("homePhone", homePhone),
            workPhone == null ? null : new BasicNameValuePair("workPhone", workPhone)
        );
    }
}
