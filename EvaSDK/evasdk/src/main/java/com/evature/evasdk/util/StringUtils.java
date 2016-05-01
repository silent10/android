package com.evature.evasdk.util;

import android.text.TextUtils;

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
}
