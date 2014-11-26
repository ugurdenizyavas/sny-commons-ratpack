package com.sony.ebs.octopus3.commons.ratpack.encoding

import org.apache.commons.lang.LocaleUtils

class ProductUtil {

    public static String formatLocale(String str) {
        def language = str.substring(0, 2).toLowerCase(EncodingUtil.LOCALE)
        def country = str.substring(3).toUpperCase(EncodingUtil.LOCALE)
        String localeStr = language + str.charAt(2) + country
        LocaleUtils.toLocale(localeStr)
        localeStr
    }

    public static String formatPublication(String str) {
        str?.toUpperCase(EncodingUtil.LOCALE)
    }

}
