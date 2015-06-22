/* AUTO-GENERATED FILE.  DO NOT MODIFY.
 *
 * This class was automatically generated by the FMPP tool.
 * It should not be modified by hand.
 * Changes to this file will be overwritten next time the project is built.
 *
 * To make changes edit the file at src/main/codegen/templates/com/evature/evasdk/EvaAppSetup.java
 *
 * Source last modified on Jun 2, 2015 04:27 PM IDT
 * Generated on Jun 2, 2015 05:36 PM IDT
 */


package com.evature.evasdk.appinterface;


import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.evature.evasdk.BuildConfig;
import com.evature.evasdk.evaapis.EvaComponent;
import com.evature.evasdk.evaapis.EvaException;
import com.evature.evasdk.evaapis.EvaSpeak;
import com.evature.evasdk.evaapis.EvaTextClient;
import com.evature.evasdk.evaapis.crossplatform.EvaApiReply;
import com.evature.evasdk.util.DLog;
import com.evature.evasdk.util.DownloadUrl;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Properties;

public class AppSetup {
    private static final String TAG = "AppSetup";

    // mandatory parameters:
    public static String apiKey;
    public static String siteCode;

    // optional parameters:
    public static boolean semanticHighlightingTimes = true;
    public static boolean semanticHighlightingLocations = true;
    public static boolean autoOpenMicrophone = false;  // true for hands free usage
    public static boolean locationTracking = true;     // true to enable Eva tracking location - used for understanding "home" location

    public static String deviceId;   // if you have a unique identifier for the user/device (leave null and Eva will generate an internal ID)
    public static String appVersion; // recommended - will be passed to Eva for debugging and tracking

    public static String scopeStr = null;

    public static HashMap<String, String> extraParams = new HashMap<String,String>();

    public static void setScope(AppScope... args) {
        StringBuilder builder = new StringBuilder();
        for (AppScope s : args) {
            builder.append(s.toString());
        }
        scopeStr = builder.toString();
    }

    public static void setAutoInferScope() {
        scopeStr = null;
    }



    /***
     * Setup Eva and wire the App callbacks
     * @param apiKey
     * @param siteCode
     * @param appHandler - inherits a set of interfaces from com.evature.evasdk.
     */
    public static void initEva(final String apiKey, final String siteCode, Object appHandler)  {
        AppSetup.apiKey = apiKey;
        AppSetup.siteCode = siteCode;
        EvaComponent.evaAppHandler = appHandler;
        InitResult _initHandler;
        if (appHandler instanceof InitResult) {
            _initHandler = (InitResult)appHandler;
        }
        else {
            _initHandler = new InitResult() {
                @Override
                public void initResult(String err, Exception e) {
                    if (err != null) {
                        if (e != null) {
                            DLog.w(TAG, err, e);
                        }
                        else {
                            DLog.w(TAG, err);
                        }
                    }
                }
            };
        }

        final InitResult initHandler = _initHandler;

        Thread checkVersion = new Thread(new Runnable() {
            @TargetApi(Build.VERSION_CODES.GINGERBREAD)
            @Override
            public void run() {

                // make a request to Eva to verify the apiKey/siteCode are valid
                String evatureUrl = "https://vproxy.evaws.com/v1.0?site_code="+siteCode+
                                    "&api_key="+apiKey+
                                    "&sdk_version="+EvaComponent.SDK_VERSION+
                                    "&verifyCredentials&input_text=";
                try {
                    String result = DownloadUrl.get(evatureUrl);
                    JSONObject jobj = new JSONObject(result);
                    String msg = jobj.optString("message");
                    if ("Site-Code / API-Key invalid".equals(msg)) {
                        initHandler.initResult("Site-Code / API-Key invalid - please check you have passed the correct credentials to initEva method, as provided by Evature.", null);
                        return;
                    }
                } catch (IOException e) {
                    initHandler.initResult("IOException in request to Evature: "+e.getMessage()+" - check your internet connectivity", e);
                    return;
                } catch (JSONException e) {
                    DLog.e(TAG, "JSON Exception parsing response from Evature: " + e.getMessage(), e);
                    initHandler.initResult("JSON Exception parsing response from Evature: "+e.getMessage(), e);
                    return;
                }

                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
                    initHandler.initResult("Warning: SDK version not checked due to old Android SDK", null);
                    return;
                }
                // check this version of the SDK is the latest version
                String versionUrl = "https://raw.githubusercontent.com/evature/android/master/EvaSDK/version.properties";
                try {
                    String masterVersion = null;
                    String result = DownloadUrl.get(versionUrl);
                    Properties props=new Properties();
                    props.load(new StringReader(result));
                    props.getProperty("VERSION_CODE", null);
                    if (masterVersion == null) {
                        initHandler.initResult("Master SDK version not found, please check for SDK updates at https://github.com/evature/android", null);
                    }
                    else {
                        try {
                            int code = Integer.parseInt(masterVersion);
                            if (code > BuildConfig.VERSION_CODE) {
                                Log.w(TAG, "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                                Log.w(TAG, "    A new version of Eva SDK is available   ");
                                Log.w(TAG, " Your version is: "+ BuildConfig.VERSION_NAME);
                                Log.w(TAG, " Master version is: "+ props.getProperty("VERSION_NAME"));
                                Log.i(TAG, " Download updated SDK at https://github.com/evature/android");
                                Log.w(TAG, "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                                initHandler.initResult("A new version of Eva SDK is available- \n your version: "+BuildConfig.VERSION_NAME+
                                        "  \n Master version "+ props.getProperty("VERSION_NAME")+
                                        "  \n Download updated SDK at https://github.com/evature/android", null);
                                return;
                            }
                            initHandler.initResult(null, null);
                            return;
                        }
                        catch (NumberFormatException e) {
                            DLog.w(TAG, "Error parsing code, please check for SDK updates at https://github.com/evature/android");
                            initHandler.initResult("Error parsing code, please check for SDK updates at https://github.com/evature/android, code="+masterVersion, null);
                            return;
                        }

                    }
                }
                catch (IOException e) {
                    DLog.w(TAG, "IOException in request to check SDK version: "+e.getMessage()+" - check your internet connectivity");
                }
            }
        });

        checkVersion.start();
    }

//    public static EvaSpeak

    public static void evaLogs(boolean enabled) {
        DLog.DebugMode = enabled;
    }
}
