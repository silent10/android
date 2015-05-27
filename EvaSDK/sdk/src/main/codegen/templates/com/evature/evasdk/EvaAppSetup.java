<#include "generated_file_warning.txt">

package com.evature.evasdk;

public class EvaAppSetup {
    public static final String SITE_CODE = "${site_code}";
    public static final String API_KEY = "${api_key}";

    public static final String SCOPE = "${scope.flight?string('f','')}${scope.hotel?string('h','')}${scope.vacation?string('v','')}${scope.car?string('c','')}${scope.cruise?string('r','')}${scope.ski?string('s','')}${scope.explore?string('e','')}";

    public static EvaAppInterface evaAppHandler;

    public static void initEva(EvaAppInterface handler) {
        evaAppHandler = handler;
    }
}
