package com.sony.ebs.octopus3.commons.ratpack.product.enhancer

import com.ning.http.client.Response
import com.sony.ebs.octopus3.commons.ratpack.http.ning.NingHttpClient
import groovy.util.logging.Slf4j
import ratpack.exec.ExecControl

import static ratpack.rx.RxRatpack.observe

@Slf4j
class CategoryEnhancer implements ProductEnhancer {

    String categoryServiceUrl

    NingHttpClient httpClient

    ExecControl execControl

    static String parseFeed(String sku, InputStream feed) {
        def xml = new XmlSlurper().parse(feed)
        def result = xml.depthFirst().find { it.name() == 'product' && it.text() == sku }
        result?.parent()?.parent()?.name?.text()
    }

    @Override
    public <T> rx.Observable<T> enhance(T obj) throws Exception {
        rx.Observable.from("starting").flatMap({
            def categoryReadUrl = categoryServiceUrl.replace(":publication", obj.publication).replace(":locale", obj.locale)
            log.info "category service url for $categoryReadUrl"
            httpClient.doGet(categoryReadUrl)
        }).filter({ Response response ->
            NingHttpClient.isSuccess(response, "getting octopus category feed")
        }).flatMap({ Response response ->
            observe(execControl.blocking {
                obj.category = parseFeed(obj.sku, response.responseBodyAsStream)
                obj
            })
        })
    }
}
