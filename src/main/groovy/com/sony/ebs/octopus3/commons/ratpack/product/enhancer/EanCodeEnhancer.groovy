package com.sony.ebs.octopus3.commons.ratpack.product.enhancer

import com.ning.http.client.Response
import com.sony.ebs.octopus3.commons.ratpack.http.ning.NingHttpClient
import groovy.util.logging.Slf4j
import ratpack.exec.ExecControl

import static ratpack.rx.RxRatpack.observe

@Slf4j
class EanCodeEnhancer implements ProductEnhancer {

    String serviceUrl

    NingHttpClient httpClient

    ExecControl execControl

    private String parseFeed(String sku, String feed) {
        def xml = new XmlSlurper().parseText(feed)
        def eanCode = xml.eancode?.@code?.toString() ?: null
        log.trace "ean code for $sku is $eanCode"
        eanCode
    }

    @Override
    public <T> rx.Observable<T> enhance(T obj) throws Exception {
        String sku = obj.sku.toUpperCase(Locale.US)
        def url = serviceUrl.replace(":product", sku)
        log.trace "ean code service url for $sku is $url"
        rx.Observable.from("starting").flatMap({
            httpClient.doGet(url)
        }).filter({ Response response ->
            boolean success = NingHttpClient.isSuccess(response)
            if (!success) {
                log.debug "$sku eliminated by eanCode not found in Octopus"
            }
            success
        }).flatMap({ Response response ->
            observe(execControl.blocking {
                def eanCode = parseFeed(sku, response.responseBody)
                if (eanCode) {
                    obj.eanCode = eanCode
                } else {
                    log.debug "$sku eliminated by eanCode not found in xml"
                }
                obj
            })
        })
    }
}
