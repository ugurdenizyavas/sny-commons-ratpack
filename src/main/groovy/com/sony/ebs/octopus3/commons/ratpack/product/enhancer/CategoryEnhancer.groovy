package com.sony.ebs.octopus3.commons.ratpack.product.enhancer

import com.sony.ebs.octopus3.commons.ratpack.http.ning.NingHttpClient
import groovy.util.logging.Slf4j
import ratpack.exec.ExecControl

import static ratpack.rx.RxRatpack.observe

@Slf4j
class CategoryEnhancer implements ProductEnhancer {

    String categoryServiceUrl

    NingHttpClient httpClient

    ExecControl execControl

    String parseFeed(String sku, String feed) {
        def xml = new XmlSlurper().parseText(feed)
        def result = xml.depthFirst().findAll { it.name() == 'product' && it.text() == sku }
        result ? result[0].parent()?.parent()?.name?.text() : null
    }

    @Override
    public <T> rx.Observable<T> enhance(T obj) throws Exception {

        rx.Observable.from("starting").flatMap({
            def categoryReadUrl = categoryServiceUrl.replace(":publication", obj.publication).replace(":locale", obj.locale)
            log.info "category service url for $categoryReadUrl"
            httpClient.doGet(categoryReadUrl)
        }).flatMap({ String feed ->
            observe(execControl.blocking {
                obj.category = parseFeed(obj.sku, feed)
                obj
            })
        })
    }
}
