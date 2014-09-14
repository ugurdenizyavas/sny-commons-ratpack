package com.sony.ebs.octopus3.commons.ratpack.product.cadc.delta.service

import com.ning.http.client.Response
import com.sony.ebs.octopus3.commons.ratpack.encoding.EncodingUtil
import com.sony.ebs.octopus3.commons.ratpack.handlers.HandlerUtil
import com.sony.ebs.octopus3.commons.ratpack.http.ning.NingHttpClient
import com.sony.ebs.octopus3.commons.ratpack.product.cadc.delta.model.Delta
import groovy.json.JsonException
import groovy.json.JsonSlurper
import groovy.util.logging.Slf4j
import ratpack.exec.ExecControl

import static ratpack.rx.RxRatpack.observe

@Slf4j
abstract class AbstractDeltaService {

    final JsonSlurper jsonSlurper = new JsonSlurper()

    ExecControl execControl

    NingHttpClient localHttpClient
    NingHttpClient cadcHttpClient

    DeltaUrlHelper deltaUrlHelper

    String cadcsourceSheetServiceUrl

    void setUrlList(Delta delta, InputStream feedInputStream) throws Exception {
        try {
            log.debug "starting creating url list"
            def json = jsonSlurper.parse(feedInputStream, EncodingUtil.CHARSET_STR)
            delta.urlList = json.skus[delta.locale].collect { it }
            log.debug "finished creating url list {}", delta.urlList
        } catch (JsonException e) {
            throw new Exception("error parsing cadc delta json", e)
        }
    }

    abstract Object createServiceResult(Response response, String cadcUrl)

    abstract Object createServiceResultOnError(String error, String cadcUrl)

    rx.Observable<String> importSingleSheet(Delta delta, String cadcUrl) {
        rx.Observable.from("starting").flatMap({
            def importUrl = cadcsourceSheetServiceUrl.replace(":publication", delta.publication).replace(":locale", delta.locale) + "?url=$cadcUrl"
            if (delta.processId?.id) importUrl += "&processId=${delta.processId?.id}"
            localHttpClient.doGet(importUrl)
        }).flatMap({ Response response ->
            observe(execControl.blocking({
                createServiceResult(response, cadcUrl)
            }))
        }).onErrorReturn({
            log.error "error for $cadcUrl", it
            def error = HandlerUtil.getErrorMessage(it)
            createServiceResultOnError(error, cadcUrl)
        })
    }

    rx.Observable deltaFlow(Delta delta) {

        rx.Observable.from("starting").flatMap({
            deltaUrlHelper.createSinceValue(delta)
        }).flatMap({ String since ->
            delta.finalSince = since
            deltaUrlHelper.createDeltaUrl(delta.cadcUrl, delta.locale, since)
        }).flatMap({ String deltaUrl ->
            delta.finalCadcUrl = deltaUrl
            cadcHttpClient.doGet(deltaUrl)
        }).filter({ Response response ->
            NingHttpClient.isSuccess(response, "getting delta json from cadc", delta.errors)
        }).flatMap({ Response response ->
            observe(execControl.blocking({
                this.setUrlList(delta, response.responseBodyAsStream)
            }))
        }).flatMap({
            deltaUrlHelper.updateLastModified(delta)
        }).flatMap({
            rx.Observable.merge(
                    delta.urlList?.collect {
                        this.importSingleSheet(delta, it)
                    }
                    , 30)
        })
    }

}

