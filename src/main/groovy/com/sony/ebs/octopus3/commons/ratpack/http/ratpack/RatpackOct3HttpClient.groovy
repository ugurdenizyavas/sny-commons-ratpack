package com.sony.ebs.octopus3.commons.ratpack.http.ratpack

import com.sony.ebs.octopus3.commons.ratpack.encoding.EncodingUtil
import com.sony.ebs.octopus3.commons.ratpack.http.Oct3HttpClient
import com.sony.ebs.octopus3.commons.ratpack.http.Oct3HttpClient.HttpMethod
import com.sony.ebs.octopus3.commons.ratpack.http.Oct3HttpResponse
import groovy.util.logging.Slf4j
import org.apache.commons.io.IOUtils
import ratpack.http.client.HttpClient
import ratpack.http.client.ReceivedResponse
import ratpack.http.client.RequestSpec
import ratpack.http.client.internal.DefaultHttpClient
import ratpack.launch.LaunchConfig

import static ratpack.rx.RxRatpack.observe

@Slf4j
class RatpackOct3HttpClient implements Oct3HttpClient {

    HttpClient httpClient
    String authenticationUser, authenticationPassword
    int connectionTimeout, readTimeout

    public RatpackOct3HttpClient(LaunchConfig launchConfig,
                                 String authenticationUser, String authenticationPassword,
                                 int connectionTimeout, int readTimeout) {
        httpClient = new DefaultHttpClient(launchConfig)
    }

    public RatpackOct3HttpClient(LaunchConfig launchConfig) {
        this(launchConfig, '', '', 0, 0)
    }

    rx.Observable<Oct3HttpResponse> doRequest(String url, HttpMethod httpMethod, byte[] byteArray = null) throws Exception {
        URI uri
        observe(
                httpClient.request({ RequestSpec request ->
                    uri = url.toURI()
                    request.headers.add('Accept-Charset', 'UTF-8')
                    request.url.set(uri)
                    request.method(httpMethod.toString())

                    if (httpMethod == HttpMethod.POST) {
                        request.headers.add('Content-Type', 'multipart/form-data')
                        request.body.stream({ OutputStream outputStream ->
                            try {
                                outputStream.write(byteArray)
                                outputStream.close()
                            } catch (IOException ex) {
                                log.error "error writing request body", ex
                            }
                        })
                    }

                    log.info "starting {} {}", httpMethod, url
                })
        ).map { ReceivedResponse resp ->
            log.info "HTTP {} {} {}", resp.statusCode, httpMethod, url
            new Oct3HttpResponse(uri: uri, statusCode: resp.statusCode, bodyAsBytes: resp.body.getBytes())
        }
    }

    @Override
    rx.Observable<Oct3HttpResponse> doPost(String url, InputStream inputStream) throws Exception {
        doRequest(url, HttpMethod.POST, IOUtils.toByteArray(inputStream))
    }

    @Override
    rx.Observable<Oct3HttpResponse> doPost(String url, String text) throws Exception {
        doRequest(url, HttpMethod.POST, text.getBytes(EncodingUtil.CHARSET))
    }

    @Override
    rx.Observable<Oct3HttpResponse> doPost(String url, byte[] byteArray) throws Exception {
        doRequest(url, HttpMethod.POST, byteArray)
    }

    @Override
    rx.Observable<Oct3HttpResponse> doGet(String url) throws Exception {
        doRequest(url, HttpMethod.GET)
    }

    @Override
    rx.Observable<Oct3HttpResponse> doDelete(String url) throws Exception {
        doRequest(url, HttpMethod.DELETE)
    }
}
