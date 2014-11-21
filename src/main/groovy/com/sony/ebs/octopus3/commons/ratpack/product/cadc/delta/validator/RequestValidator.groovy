package com.sony.ebs.octopus3.commons.ratpack.product.cadc.delta.validator

import com.sony.ebs.octopus3.commons.date.ISODateUtils
import com.sony.ebs.octopus3.commons.flows.Delta
import com.sony.ebs.octopus3.commons.ratpack.product.cadc.delta.model.CadcDelta
import com.sony.ebs.octopus3.commons.ratpack.product.cadc.delta.model.CadcProduct
import com.sony.ebs.octopus3.commons.ratpack.product.cadc.delta.model.RepoDelta
import com.sony.ebs.octopus3.commons.ratpack.product.cadc.delta.model.RepoProduct
import groovy.util.logging.Slf4j
import org.apache.commons.lang.LocaleUtils
import org.apache.http.client.utils.URIBuilder

@Slf4j
class RequestValidator {

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

    void validateDate(String name, String value, List errors) {
        try {
            ISODateUtils.toISODate(value)
        } catch (e) {
            errors << "$name parameter is invalid".toString()
        }
    }

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
    List validateCadcDelta(CadcDelta delta) {
        List errors = []

        if (!delta.type) {
            errors << "type parameter is invalid"
        }
        validatePublication(delta.publication, errors)
        validateLocale(delta.locale, errors)

        if (delta.since && !delta.since.equalsIgnoreCase("all")) {
            validateDate("since", delta.since, errors)
        }

        if (!validateUrl(delta.cadcUrl)) {
            errors << "cadcUrl parameter is invalid"
        }
        errors
    }

    List validateRepoDelta(Delta delta) {
        List errors = []

        if (!delta.type) {
            errors << "type parameter is invalid"
        }
        validatePublication(delta.publication, errors)
        validateLocale(delta.locale, errors)
        if (delta.sdate) validateDate("sdate", delta.sdate, errors)
        if (delta.edate) validateDate("edate", delta.edate, errors)
        errors
    }

    /**
     * Validates all deltaSheet params
     * @param product
     * @return
     */
    List validateCadcProduct(CadcProduct product) {
        List errors = []

        if (!product.type) {
            errors << "type parameter is invalid"
        }
        validatePublication(product.publication, errors)
        validateLocale(product.locale, errors)
        if (!validateUrl(product.url)) {
            errors << "url parameter is invalid"
        }
        errors
    }

    List validateRepoProduct(RepoProduct product) {
        List errors = []

        if (!product.type) {
            errors << "type parameter is invalid"
        }
        validatePublication(product.publication, errors)
        validateLocale(product.locale, errors)
        if (!product.sku) {
            errors << "sku parameter is invalid"
        }
        errors
    }


}
