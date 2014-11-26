package com.sony.ebs.octopus3.commons.ratpack.file

import com.sony.ebs.octopus3.commons.flows.Delta
import com.sony.ebs.octopus3.commons.urn.URNCreationException
import com.sony.ebs.octopus3.commons.ratpack.http.Oct3HttpClient
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

    Oct3HttpClient httpClient
    String saveUrl

    /**
     * Stores given response into repository to path [/responses/{urnValues}/{processId]] if delta.upload is true
     *
     * @param delta Delta object of the flow
     * @param response response to save
     * @return true if response is saved successfully
     */
    boolean store(Delta delta, String response) {
        if (delta.upload) {
            def urnValues = [delta.flow?.toString()?.toLowerCase(), delta.service?.toString()?.toLowerCase(), delta.publication, delta.locale, delta.processId?.id]
            try {
                def url = new URIBuilder(saveUrl.replace(":urn", new URNImpl("responses", urnValues).toString())).addQueryParam("processId", delta.processId.id).toString()

                httpClient.doPost(url, IOUtils.toInputStream(response, Charset.forName('UTF-8'))).subscribe(
                        {
                            log.debug "Response is sent to repository with url: {}", url
                            it.success
                        },
                        { e ->
                            log.error "Error in sending response to repository with url: ${url}", e
                            false
                        }
                )
            } catch (URNCreationException e) {
                log.warn "Cannot write response to repository since urn parameters are invalid", e
            }
        }
    }
}
