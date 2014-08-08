package com.sony.ebs.octopus3.commons.ratpack.file

import com.ning.http.client.Response
import com.sony.ebs.octopus3.commons.ratpack.http.ning.NingHttpClient
import com.sony.ebs.octopus3.commons.urn.URN
import groovy.json.JsonSlurper
import groovy.util.logging.Slf4j
import ratpack.exec.ExecControl

import static ratpack.rx.RxRatpack.observe

@Slf4j
class FileAttributesProvider {

    ExecControl execControl

    String repositoryFileAttributesServiceUrl

    NingHttpClient httpClient

    rx.Observable<FileAttribute> getLastModifiedTime(URN urn) {
        rx.Observable.just("starting").flatMap({
            def url = repositoryFileAttributesServiceUrl.replace(":urn", urn.toString())
            httpClient.doGet(url)
        }).flatMap({ Response response ->
            if (NingHttpClient.isSuccess(response)) {
                observe(execControl.blocking {
                    def json = new JsonSlurper().parseText(response.responseBody)
                    def lastModifiedTime = json.result.lastModifiedTime
                    log.info "lastModifiedTime for $urn is $lastModifiedTime"
                    return new FileAttribute(found: true, value: lastModifiedTime)
                })
            } else {
                return rx.Observable.just(new FileAttribute(found: false))
            }
        })
    }

}
