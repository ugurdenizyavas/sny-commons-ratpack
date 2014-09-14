package com.sony.ebs.octopus3.commons.ratpack.product.cadc.delta.validator

import com.sony.ebs.octopus3.commons.date.ISODateUtils
import com.sony.ebs.octopus3.commons.ratpack.product.cadc.delta.model.Delta
import com.sony.ebs.octopus3.commons.ratpack.product.cadc.delta.model.DeltaItem
import groovy.util.logging.Slf4j
import org.apache.commons.lang.LocaleUtils
import org.apache.http.client.utils.URIBuilder
import org.springframework.stereotype.Component

@Slf4j
class RequestValidator {

    void validatePublication(String publication, List errors) {
        if (!(publication ==~ /[a-zA-Z0-9\-\_]+/)) {
            errors << "publication parameter is invalid"
        }
    }

    void validateLocale(String locale, List errors) {
        try {
            if (!locale) {
                errors << "locale parameter is invalid"
            } else {
                LocaleUtils.toLocale(locale)
            }
        } catch (e) {
            errors << "locale parameter is invalid"
        }
    }

    /**
     * Validates all delta params
     * @param delta
     * @return
     */
    List validateDelta(Delta delta) {
        List errors = []

        if (!delta.type) {
            errors << "type parameter is invalid"
        }
        validatePublication(delta.publication, errors)
        validateLocale(delta.locale, errors)
        try {
            if (delta.since && !delta.since.equalsIgnoreCase("all")) {
                ISODateUtils.toISODate(delta.since)
            }
        } catch (e) {
            errors << "since parameter is invalid"
        }
        if (!validateUrl(delta.cadcUrl)) {
            errors << "cadcUrl parameter is invalid"
        }
        errors
    }

    /**
     * Validates all deltaSheet params
     * @param deltaItem
     * @return
     */
    List validateDeltaItem(DeltaItem deltaItem) {
        List errors = []

        if (!deltaItem.type) {
            errors << "type parameter is invalid"
        }
        validatePublication(deltaItem.publication, errors)
        validateLocale(deltaItem.locale, errors)
        if (!validateUrl(deltaItem.url)) {
            errors << "url parameter is invalid"
        }
        errors
    }

    /**
     * The url needs to be valid and should have a host
     * @param url
     * @return
     */
    private boolean validateUrl(String url) {
        if (url) {
            URIBuilder uriBuilder
            try {
                uriBuilder = new URIBuilder(url)
            } catch (e) {
                log.error "invalid url value $url", e
                return false
            }
            uriBuilder.host
        } else {
            false
        }
    }

}
