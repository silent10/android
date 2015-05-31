<#include "generated_file_warning.txt">

package com.evature.evasdk;

    import com.evature.evasdk.evaapis.android.EvaComponent;

public class EvaAppSetup {
    public static EvaAppInterface evaAppHandler;
    public static void initEva(EvaAppInterface handler) {
        evaAppHandler = handler;
    }

    public static final boolean SEMANTIC_HIGHLIGHTING = ${semantic_highlighting?has_content?c};
    public static final boolean SEMANTIC_HIGHLIGHT_TIMES = ${(semantic_highlighting.times)?c};
    public static final boolean SEMANTIC_HIGHLIGHT_LOCATIONS = ${(semantic_highlighting.locations)?c};

    public static final boolean AUTO_OPEN_MICROPHONE = ${auto_open_microphone?c};


    public static final boolean ASYNC_COUNT = ${async_count?c};

    public static void setupEva(EvaComponent.EvaConfig config) {
        config.appKey = "${api_key}";
        config.siteCode = "${site_code}";
        config.scope = "${scope.flight?string('f','')}${scope.hotel?string('h','')}${scope.vacation?string('v','')}${scope.car?string('c','')}${scope.cruise?string('r','')}${scope.ski?string('s','')}${scope.explore?string('e','')}";
        config.context = config.scope;
        <#if url_params??>
            <#list url_params?keys as key>
        config.setParameter("${key}", "${url_params[key]}");
            </#list>
        </#if>


        <#if location_disabled?? >
        config.locationEnabled = false;
        </#if>

        if (SEMANTIC_HIGHLIGHTING) {
            config.setParameter("add_text", ""); // ask Eva to reply with Semantic highlighting meta data
        }
        if (AUTO_OPEN_MICROPHONE) {
            config.setParameter("auto_open_mic", String.valueOf(AUTO_OPEN_MICROPHONE));
        }
    }
}
