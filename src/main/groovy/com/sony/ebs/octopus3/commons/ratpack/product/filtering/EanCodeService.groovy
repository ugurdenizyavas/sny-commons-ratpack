package com.sony.ebs.octopus3.commons.ratpack.product.filtering

import com.sony.ebs.octopus3.commons.ratpack.encoding.MaterialNameEncoder
import com.sony.ebs.octopus3.commons.ratpack.http.Oct3HttpClient
import com.sony.ebs.octopus3.commons.ratpack.http.Oct3HttpResponse
import com.sony.ebs.octopus3.commons.urn.URNImpl
import groovy.util.logging.Slf4j
import ratpack.exec.ExecControl

import static ratpack.rx.RxRatpack.observe

@Slf4j
class EanCodeService {

    final XmlSlurper xmlSlurper = new XmlSlurper()

    ExecControl execControl

    String octopusEanCodeServiceUrl

    Oct3HttpClient httpClient

    rx.Observable<String> filterForEanCodes(List productUrls, List errors) {
        rx.Observable.just("starting").flatMap({
            httpClient.doGet(octopusEanCodeServiceUrl)
        }).filter({ Oct3HttpResponse response ->
            response.isSuccessful("getting ean code feed", errors)
        }).flatMap({ Oct3HttpResponse response ->
            observe(execControl.blocking({
                def productMap = [:]
                productUrls.each {
                    def sku = new URNImpl(it).values?.last()
                    sku = MaterialNameEncoder.decode(sku)
                    productMap[sku] = it
                }

                def xml = xmlSlurper.parse(response.bodyAsStream)
                Map eanCodeMap = [:]
                xml.identifier?.each { identifier ->
                    def key = identifier.@materialName?.toString()?.toUpperCase(MaterialNameEncoder.LOCALE)
                    if (productMap[key]) {
                        eanCodeMap[productMap[key]] = identifier.text()
                    }
                }
                log.info "finished eanCode filtering: {} left, from {}", eanCodeMap?.size(), productUrls?.size()
                "done"
                eanCodeMap
            }))
        })
    }

}
