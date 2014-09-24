package com.sony.ebs.octopus3.commons.ratpack.product.cadc.delta.service

import com.ning.http.client.Response
import com.sony.ebs.octopus3.commons.ratpack.file.FileAttributesProvider
import com.sony.ebs.octopus3.commons.ratpack.http.ning.NingHttpClient
import com.sony.ebs.octopus3.commons.urn.URN
import groovy.util.logging.Slf4j
import org.apache.http.client.utils.URIBuilder
import ratpack.exec.ExecControl

import static ratpack.rx.RxRatpack.observe

@Slf4j
class DeltaUrlHelper {

    ExecControl execControl

    String repositoryFileServiceUrl

    NingHttpClient httpClient

    FileAttributesProvider fileAttributesProvider

    rx.Observable<String> updateLastModified(URN lastModifiedUrn, List errors) {
        rx.Observable.just("starting").flatMap({
            def url = repositoryFileServiceUrl.replace(":urn", lastModifiedUrn?.toString())
            httpClient.doPost(url, "update")
        }).filter({ Response response ->
            NingHttpClient.isSuccess(response, "updating last modified date", errors)
        }).map({
            "done"
        })
    }

    rx.Observable<String> createCadcDeltaUrl(String cadcUrl, String locale, String since) {
        observe(execControl.blocking({
            def url
            if (!since || since.equalsIgnoreCase("all")) {
                url = new URIBuilder("$cadcUrl/$locale").toString()
            } else {
                def urlBuilder = new URIBuilder("$cadcUrl/changes/$locale")
                urlBuilder.addParameter("since", since)
                url = urlBuilder.toString()
            }
            log.info "url inner for locale {} and since {} is {}", locale, since, url
            url
        }))
    }

    rx.Observable<String> createSinceValue(String since, URN lastModifiedUrn) {
        if (since) {
            rx.Observable.just(since)
        } else {
            fileAttributesProvider.getLastModifiedTime(lastModifiedUrn)
                    .map({ result ->
                result.found ? result.value : ""
            })
        }
    }

    rx.Observable<String> createRepoDeltaUrl(String initialUrl, String sdate, String edate) {
        observe(execControl.blocking({
            def uriBuilder = new URIBuilder(initialUrl)
            def addDate = { String name, String value ->
                if (value)
                    uriBuilder.addParameter(name, value)
            }
            addDate("sdate", sdate)
            addDate("edate", edate)
            uriBuilder.toString()
        }))
    }

    rx.Observable<String> createStartDate(String sdate, URN lastModifiedUrn) {
        if (sdate) {
            rx.Observable.just(sdate)
        } else {
            fileAttributesProvider.getLastModifiedTime(lastModifiedUrn).map({
                it.found ? it.value : null
            })
        }
    }

}
