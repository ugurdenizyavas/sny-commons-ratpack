package com.sony.ebs.octopus3.commons.ratpack.http.ratpack

import com.sony.ebs.octopus3.commons.ratpack.http.Oct3HttpClient
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

    public RatpackOct3HttpClient(LaunchConfig launchConfig,
                                 String authenticationUser, String authenticationPassword,
                                 int connectionTimeout, int readTimeout) {
        httpClient = new DefaultHttpClient(launchConfig)
    }

    public RatpackOct3HttpClient(LaunchConfig launchConfig) {
        this(launchConfig, '', '', 0, 0)
    }

    @Override
    rx.Observable<Oct3HttpResponse> doGet(String url) throws Exception {
        URI uri
        observe(
                httpClient.get({ RequestSpec request ->
                    uri = url.toURI()
                    request.headers.add('Accept-Charset', 'UTF-8')
                    request.url.set(uri)
                    log.info "starting GET {}", url
                })
        ).map { ReceivedResponse resp ->
            log.info "HTTP {} GET {}", resp.statusCode, url
            new Oct3HttpResponse(uri: uri, statusCode: resp.statusCode, bodyAsBytes: resp.body.getBytes())
        }
    }

    @Override
    rx.Observable<Oct3HttpResponse> doPost(String url, byte[] byteArray) throws Exception {
        URI uri
        observe(
                httpClient.post({ RequestSpec request ->
                    uri = url.toURI()
                    request.headers.add('Accept-Charset', 'UTF-8')
                    request.headers.add('Content-Type', 'multipart/form-data')
                    request.body.stream({ OutputStream outputStream ->
                        try {
                            outputStream.write(byteArray)
                            outputStream.close()
                        } catch (IOException ex) {
                            log.error "error writing request body", ex
                        }
                    })
                    request.url.set(uri)
                    log.info "starting POST {}", url
                })
        ).map { ReceivedResponse resp ->
            log.info "HTTP {} POST {}", resp.statusCode, url
            new Oct3HttpResponse(uri: uri, statusCode: resp.statusCode, bodyAsBytes: resp.body.getBytes())
        }
    }

    @Override
    rx.Observable<Oct3HttpResponse> doPost(String url, InputStream inputStream) throws Exception {
        doPost(url, IOUtils.toByteArray(inputStream))
    }

}
