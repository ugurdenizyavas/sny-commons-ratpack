package com.sony.ebs.octopus3.commons.ratpack.handlers

import org.joda.time.DateTime
import org.joda.time.Period
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import org.joda.time.format.PeriodFormatter
import org.joda.time.format.PeriodFormatterBuilder

class HandlerUtil {

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

}
