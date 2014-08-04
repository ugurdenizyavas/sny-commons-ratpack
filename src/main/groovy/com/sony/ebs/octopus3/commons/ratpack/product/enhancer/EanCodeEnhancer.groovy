package com.sony.ebs.octopus3.commons.ratpack.product.enhancer

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
        log.info "parsing eanCode xml"
        def xml = new XmlSlurper().parseText(feed)
        def eanCode = xml.eancode?.@code?.toString() ?: null
        log.info "ean code for $sku is $eanCode"
        eanCode
    }

    @Override
    public <T> rx.Observable<T> enhance(T obj) throws Exception {
        String sku = obj.sku.toUpperCase(Locale.US)
        def url = serviceUrl.replace(":product", sku)
        log.info "ean code service url for $sku is $url"
        rx.Observable.from("starting").flatMap({
            httpClient.doGet(url)
        }).flatMap({ String feed ->
            observe(execControl.blocking {
                obj.eanCode = parseFeed(sku, feed)
                obj
            })
        })
    }
}
