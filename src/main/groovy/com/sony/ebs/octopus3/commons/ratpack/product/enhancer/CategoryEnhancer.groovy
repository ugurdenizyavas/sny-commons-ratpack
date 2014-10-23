package com.sony.ebs.octopus3.commons.ratpack.product.enhancer

import com.sony.ebs.octopus3.commons.ratpack.encoding.MaterialNameEncoder
import com.sony.ebs.octopus3.commons.ratpack.http.Oct3HttpClient
import com.sony.ebs.octopus3.commons.ratpack.http.Oct3HttpResponse
import groovy.util.logging.Slf4j
import ratpack.exec.ExecControl

import static ratpack.rx.RxRatpack.observe

@Slf4j
class CategoryEnhancer implements ProductEnhancer {

    final XmlSlurper xmlSlurper = new XmlSlurper()

    String categoryServiceUrl

    Oct3HttpClient httpClient

    ExecControl execControl

    String parseFeed(String sku, boolean encoded, InputStream feed) {
        String product = encoded ? MaterialNameEncoder.decode(sku) : sku?.toUpperCase(MaterialNameEncoder.LOCALE)
        def xml = xmlSlurper.parse(feed)
        def result = xml.depthFirst().find {
            'product'.equalsIgnoreCase(it.name()) && product.equalsIgnoreCase(it.text())
        }
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
        enhance(obj, false)
    }

    @Override
    def <T> rx.Observable<T> enhance(T obj, boolean encoded) throws Exception {
        rx.Observable.from("starting").flatMap({
            def categoryReadUrl = generateUrl(obj.publication, obj.locale)
            log.info "category service url for {}", categoryReadUrl
            httpClient.doGet(categoryReadUrl)
        }).filter({ Oct3HttpResponse response ->
            response.success
        }).flatMap({ Oct3HttpResponse response ->
            observe(execControl.blocking {
                obj.category = parseFeed(obj.sku, encoded, response.bodyAsStream)
                obj
            })
        })
    }
}
