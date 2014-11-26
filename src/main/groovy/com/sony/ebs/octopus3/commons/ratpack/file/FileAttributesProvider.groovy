package com.sony.ebs.octopus3.commons.ratpack.file

import com.sony.ebs.octopus3.commons.ratpack.http.Oct3HttpClient
import com.sony.ebs.octopus3.commons.ratpack.http.Oct3HttpResponse
import com.sony.ebs.octopus3.commons.urn.URN
import groovy.json.JsonSlurper
import groovy.util.logging.Slf4j
import ratpack.exec.ExecControl

import static ratpack.rx.RxRatpack.observe

@Slf4j
class FileAttributesProvider {

    final JsonSlurper jsonSlurper = new JsonSlurper()

    ExecControl execControl
    Oct3HttpClient httpClient

    String repositoryFileAttributesServiceUrl
    String repositoryFileSaveUrl

    rx.Observable<FileAttribute> getLastModifiedTime(URN urn) {
        rx.Observable.just("starting").flatMap({
            def url = repositoryFileAttributesServiceUrl.replace(":urn", urn.toString())
            httpClient.doGet(url)
        }).flatMap({ Oct3HttpResponse response ->
            if (response.success) {
                observe(execControl.blocking {
                    def text = new String(response.bodyAsBytes, "UTF-8")
                    def json = jsonSlurper.parseText(text)
                    def lastModifiedTime = json.result?.lastModifiedTime
                    log.info "lastModifiedTime for {} is {}", urn, lastModifiedTime
                    return new FileAttribute(found: true, value: lastModifiedTime)
                })
            } else {
                return rx.Observable.just(new FileAttribute(found: false))
            }
        })
    }

    rx.Observable<Boolean> updateLastModifiedTime(URN urn) {
        def url = repositoryFileSaveUrl.replace(":urn", urn.toString())
        httpClient.doPost(url, "update").map({ Oct3HttpResponse response ->
            response.success
        })
    }

}
