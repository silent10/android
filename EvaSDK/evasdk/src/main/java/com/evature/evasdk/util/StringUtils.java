package com.evature.evasdk.util;

import android.text.TextUtils;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;

/**
 * Created by iftah on 11/04/2016.
 */
public class StringUtils {

    public static String toCamelCase(String str, String... separators) {
        if (separators.length == 0) {
            separators = new String[] { " ", "_", "-", ","};
        }
        String separatorsRegex = "\\".concat(TextUtils.join("|\\", separators));
        String[] splits = str.toLowerCase().split(separatorsRegex);
        StringBuffer sb = new StringBuffer();
        for (String token : splits) {
            sb.append(token.substring(0, 1).toUpperCase() + token.substring(1));
        }
        return sb.toString();
    }

    private static final String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static SecureRandom rnd = new SecureRandom();

    public static String randomString(int len) {
        StringBuilder sb = new StringBuilder( len );
        for( int i = 0; i < len; i++ )
            sb.append( AB.charAt( rnd.nextInt(AB.length()) ) );
        return sb.toString();
    }
}
