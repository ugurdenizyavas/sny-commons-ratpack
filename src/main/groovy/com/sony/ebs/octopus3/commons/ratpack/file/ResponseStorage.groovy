package com.sony.ebs.octopus3.commons.ratpack.file

import com.sony.ebs.octopus3.commons.ratpack.http.ning.NingHttpClient
import com.sony.ebs.octopus3.commons.urn.URNImpl
import groovy.util.logging.Slf4j
import groovyx.net.http.URIBuilder
import org.apache.commons.io.IOUtils

import java.nio.charset.Charset

/**
 * author: TRYavasU
 * date: 25/09/2014
 */
@Slf4j
class ResponseStorage {

    NingHttpClient ningHttpClient
    String saveUrl

    /**
     * Stores given response into repository to path [/responses/{urnValues}/{processId]]
     *
     * @param processId processId of the current job
     * @param urnValues values of the urn to copy response (urn type is responses)
     * @param response response to save
     * @return true if response is saved successfully
     */
    boolean store(String processId, ArrayList<String> urnValues, String response) {
        def url = new URIBuilder(saveUrl.replace(":urn", new URNImpl("responses", urnValues).toString())).addQueryParam("processId", processId).toString()

        ningHttpClient.doPost(url, IOUtils.toInputStream(response, Charset.forName('UTF-8')))
                .subscribe(
                {
                    log.debug "Response is sent to repository with url: {}", url
                    NingHttpClient.isSuccess(it)
                },
                { e ->
                    log.error "Error in sending response to repository with url: ${url}", e
                    false
                }
        )
    }
}