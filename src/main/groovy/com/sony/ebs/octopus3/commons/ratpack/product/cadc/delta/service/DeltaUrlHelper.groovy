package com.sony.ebs.octopus3.commons.ratpack.product.cadc.delta.service

import com.ning.http.client.Response
import com.sony.ebs.octopus3.commons.ratpack.encoding.EncodingUtil
import com.sony.ebs.octopus3.commons.ratpack.file.FileAttributesProvider
import com.sony.ebs.octopus3.commons.ratpack.http.ning.NingHttpClient
import com.sony.ebs.octopus3.commons.ratpack.product.cadc.delta.model.Delta
import groovy.util.logging.Slf4j
import ratpack.exec.ExecControl

import static ratpack.rx.RxRatpack.observe

@Slf4j
class DeltaUrlHelper {

    ExecControl execControl

    String repositoryFileServiceUrl

    NingHttpClient httpClient

    FileAttributesProvider fileAttributesProvider

    rx.Observable<String> updateLastModified(Delta delta) {
        rx.Observable.just("starting").flatMap({
            def url = repositoryFileServiceUrl.replace(":urn", delta.lastModifiedUrn?.toString())
            httpClient.doPost(url, "update")
        }).filter({ Response response ->
            NingHttpClient.isSuccess(response, "updating last modified date", delta.errors)
        }).map({
            "done"
        })
    }

    rx.Observable<String> createDeltaUrl(String cadcUrl, String locale, String since) {
        observe(execControl.blocking({
            def url
            if (!since || since.equalsIgnoreCase("all")) {
                url = "$cadcUrl/$locale"
            } else {
                url = "$cadcUrl/changes/$locale?since=" + URLEncoder.encode(since, EncodingUtil.CHARSET_STR)
            }
            log.info "url inner for locale {} and since {} is {}", locale, since, url
            url
        }))
    }

    rx.Observable<String> createSinceValue(Delta delta) {
        if (delta.since) {
            rx.Observable.just(delta.since)
        } else {
            fileAttributesProvider.getLastModifiedTime(delta.lastModifiedUrn)
                    .map({ result ->
                result.found ? result.value : ""
            })
        }
    }

}
