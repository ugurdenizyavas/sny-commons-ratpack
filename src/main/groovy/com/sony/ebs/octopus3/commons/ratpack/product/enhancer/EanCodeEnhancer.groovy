package com.sony.ebs.octopus3.commons.ratpack.product.enhancer

import com.sony.ebs.octopus3.commons.ratpack.encoding.MaterialNameEncoder
import com.sony.ebs.octopus3.commons.ratpack.http.Oct3HttpClient
import com.sony.ebs.octopus3.commons.ratpack.http.Oct3HttpResponse
import groovy.util.logging.Slf4j
import ratpack.exec.ExecControl

import static ratpack.rx.RxRatpack.observe

@Slf4j
class EanCodeEnhancer implements ProductEnhancer {

    String serviceUrl

    Oct3HttpClient httpClient

    ExecControl execControl

    public static String parseFeed(String sku, InputStream feed) {
        def xml = new XmlSlurper().parse(feed)
        def eanCode = xml.product?.identifier?.find({ it['@type'] == 'ean_code' })?.text()
        log.trace "ean code for {} is {}", sku, eanCode
        eanCode
    }

    @Override
    def <T> rx.Observable<T> enhance(T obj, boolean encoded = false) throws Exception {
        String sku
        if (obj instanceof Map) {
            sku = obj.sku ?: obj.materialName
        } else if (obj.hasProperty("sku")) {
            sku = obj.sku
        } else if (obj.hasProperty("materialName")) {
            sku = obj.materialName
        }
        String product = encoded ? sku?.toUpperCase(MaterialNameEncoder.LOCALE) : MaterialNameEncoder.encode(sku)
        def url = serviceUrl.replace(":product", product)
        log.trace "ean code service url for {} is {}", sku, url
        rx.Observable.from("starting").flatMap({
            httpClient.doGet(url)
        }).flatMap({ Oct3HttpResponse response ->
            if (!response.success) {
                log.debug "{} eliminated by eanCode not found in Octopus", sku
                rx.Observable.just(obj)
            } else {
                observe(execControl.blocking {
                    def eanCode = parseFeed(sku, response.bodyAsStream)
                    if (eanCode) {
                        obj.eanCode = eanCode
                    } else {
                        log.debug "{} eliminated by eanCode not found in xml", sku
                    }
                    obj
                })
            }
        })
    }
}