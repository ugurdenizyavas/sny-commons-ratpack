package com.sony.ebs.octopus3.commons.ratpack.handlers

import com.sony.ebs.octopus3.commons.ratpack.encoding.EncodingUtil
import com.sony.ebs.octopus3.commons.ratpack.http.Oct3HttpResponse
import groovy.json.JsonSlurper
import groovyx.net.http.URIBuilder
import org.joda.time.DateTime
import org.joda.time.Period
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import org.joda.time.format.PeriodFormatter
import org.joda.time.format.PeriodFormatterBuilder

class HandlerUtil {

    final static JsonSlurper jsonSlurper = new JsonSlurper()

    static PeriodFormatter PERIOD_FORMATTER = new PeriodFormatterBuilder()
            .printZeroAlways()
            .minimumPrintedDigits(2)
            .maximumParsedDigits(2)
            .appendMinutes()
            .appendSeparator(":")
            .printZeroAlways()
            .minimumPrintedDigits(2)
            .maximumParsedDigits(2)
            .appendSeconds()
            .appendSeparator(":")
            .printZeroAlways()
            .minimumPrintedDigits(3)
            .maximumParsedDigits(3)
            .appendMillis3Digit()
            .toFormatter()

    static DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormat.forPattern("yyyy-MM-dd, HH:mm:ss.SSS, Z")

    static String getErrorMessage(Throwable t) {
        (t?.message ?: t?.cause?.message)?.toString()
    }

    static Map getTimeStats(DateTime startTime, DateTime endTime) {
        Period period = new Period(startTime, endTime);
        [
                start   : DATE_TIME_FORMATTER.print(startTime),
                end     : DATE_TIME_FORMATTER.print(endTime),
                duration: PERIOD_FORMATTER.print(period)
        ]
    }

    static String addProcessId(String initialUrl, String processId) {
        new URIBuilder(initialUrl).with {
            if (processId) {
                addQueryParam("processId", processId)
            }
            it.toString()
        }
    }

    static Object parseOct3ResponseQuiet(Oct3HttpResponse response) {
        def json
        try {
            json = jsonSlurper.parse(response.bodyAsStream, EncodingUtil.CHARSET_STR)
        } catch (all) {
            json = [:]
        }
        json
    }
}
