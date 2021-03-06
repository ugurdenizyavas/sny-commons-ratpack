package com.sony.ebs.octopus3.commons.ratpack.product.filtering

import com.sony.ebs.octopus3.commons.flows.RepoValue
import com.sony.ebs.octopus3.commons.ratpack.encoding.EncodingUtil
import com.sony.ebs.octopus3.commons.ratpack.encoding.MaterialNameEncoder
import com.sony.ebs.octopus3.commons.ratpack.encoding.ProductUtil
import com.sony.ebs.octopus3.commons.ratpack.handlers.HandlerUtil
import com.sony.ebs.octopus3.commons.ratpack.http.Oct3HttpClient
import com.sony.ebs.octopus3.commons.ratpack.http.Oct3HttpResponse
import com.sony.ebs.octopus3.commons.ratpack.product.cadc.delta.model.RepoDelta
import com.sony.ebs.octopus3.commons.urn.URN
import com.sony.ebs.octopus3.commons.urn.URNImpl
import groovy.util.logging.Slf4j
import org.apache.commons.io.IOUtils
import ratpack.exec.ExecControl

import static ratpack.rx.RxRatpack.observe

@Slf4j
class CategoryService {

    final XmlSlurper xmlSlurper = new XmlSlurper()

    ExecControl execControl

    String octopusCategoryServiceUrl

    String repositoryFileServiceUrl

    Oct3HttpClient httpClient

    URN getCategoryUrn(RepoDelta delta) {
        new URNImpl(delta.type.toString(), [delta.publication, delta.locale, RepoValue.category.toString() + ".xml"])
    }

    rx.Observable<String> saveCategoryFeed(RepoDelta delta, String categoryFeed, List errors = []) {
        rx.Observable.just("starting").flatMap({
            observe(execControl.blocking {
                def urn = new URNImpl(delta.type.toString(), [delta.publication, delta.locale, RepoValue.category.toString() + ".xml"])
                def initialUrl = repositoryFileServiceUrl.replace(":urn", urn.toString())
                log.info "category save url for {} is {}", delta, initialUrl
                HandlerUtil.addProcessId(initialUrl, delta.processId?.id)
            })
        }).flatMap({ url ->
            httpClient.doPost(url, IOUtils.toInputStream(categoryFeed, EncodingUtil.CHARSET))
        }).filter({ Oct3HttpResponse response ->
            response.isSuccessful("saving octopus category feed", errors)
        }).map({
            categoryFeed
        })
    }

    rx.Observable<String> retrieveCategoryFeed(String publication, String locale, List errors = []) {
        rx.Observable.just("starting").flatMap({
            observe(execControl.blocking {
                def urlPublication = ProductUtil.formatPublication(publication)
                def urlLocale = ProductUtil.formatLocale(locale)
                def categoryReadUrl = octopusCategoryServiceUrl.replace(":publication", urlPublication).replace(":locale", urlLocale)
                log.info "category service url for {} {} is {}", publication, locale, categoryReadUrl
                categoryReadUrl
            })
        }).flatMap({ url ->
            httpClient.doGet(url)
        }).filter({ Oct3HttpResponse response ->
            response.isSuccessful("getting octopus category feed", errors)
        }).map({ Oct3HttpResponse response ->
            def categoryFeed = IOUtils.toString(response.bodyAsStream, EncodingUtil.CHARSET)
            categoryFeed
        })
    }

    rx.Observable<Map> filterForCategory(List productUrls, String categoryFeed) {
        observe(execControl.blocking {
            log.info "starting category filtering"
            def categoryXml = xmlSlurper.parseText(categoryFeed)

            Map categoryMap = [:]
            categoryXml.depthFirst().findAll({ it.name() == 'product' }).collect({
                def key = it.text()?.toUpperCase(MaterialNameEncoder.LOCALE)
                def value = it.parent()?.parent()?.name.text()
                categoryMap[key] = value
            })

            Map filteredCategoryMap = [:]
            productUrls.each {
                String urnStr = it.toString()
                def sku = new URNImpl(urnStr).values?.last()
                sku = MaterialNameEncoder.decode(sku)
                def category = categoryMap[sku]
                if (category) {
                    filteredCategoryMap[urnStr] = category
                }
            }

            log.info "finished category filtering: {} left, from {}", filteredCategoryMap.size(), productUrls?.size()
            filteredCategoryMap
        })
    }

}
