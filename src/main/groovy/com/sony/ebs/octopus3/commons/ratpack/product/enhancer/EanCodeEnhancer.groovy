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

    public static String parseFeed(String sku, InputStream feed) {
        def xml = new XmlSlurper().parse(feed)
        def eanCode = xml.product?.identifier?.find({ it['@type'] == 'ean_code' })?.text()
        log.trace "ean code for {} is {}", sku, eanCode
        eanCode
    }

    @Override
    public <T> rx.Observable<T> enhance(T obj) throws Exception {
        String sku = obj.sku.toUpperCase(Locale.US)
        def url = serviceUrl.replace(":product", sku)
        log.trace "ean code service url for {} is {}", sku, url
        rx.Observable.from("starting").flatMap({
            httpClient.doGet(url)
        }).flatMap({ Response response ->
            boolean found = NingHttpClient.isSuccess(response, "getting ean code")
            if (!found) {
                log.debug "{} eliminated by eanCode not found in Octopus", sku
                rx.Observable.just(obj)
            } else {
                observe(execControl.blocking {
                    def eanCode = parseFeed(sku, response.responseBodyAsStream)
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
