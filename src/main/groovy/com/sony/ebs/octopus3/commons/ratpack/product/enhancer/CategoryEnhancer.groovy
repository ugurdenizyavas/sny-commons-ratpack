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

    String generateUrl(String publication, String locale) {
        String properPublication = publication.toUpperCase(Locale.US)

        def localeParts = locale.split('_')
        localeParts[1] = localeParts[1].toUpperCase(Locale.US)
        String properLocale = localeParts.join('_')

        categoryServiceUrl.replace(
                ":publication", properPublication
        ).replace(
                ":locale", properLocale
        )
    }

    @Override
    public <T> rx.Observable<T> enhance(T obj) throws Exception {
        rx.Observable.from("starting").flatMap({
            def categoryReadUrl = generateUrl(obj.publication, obj.locale)
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
