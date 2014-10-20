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
class RatpackOdw3HttpClient implements Oct3HttpClient {

    LaunchConfig launchConfig

    HttpClient httpClient

    public RatpackOdw3HttpClient(LaunchConfig launchConfig,
                                 String authenticationUser, String authenticationPassword,
                                 int connectionTimeout, int readTimeout) {
        httpClient = new DefaultHttpClient(launchConfig)
    }

    public RatpackOdw3HttpClient(LaunchConfig launchConfig) {
        this(launchConfig, '', '', 0, 0)
    }

    @Override
    rx.Observable<Oct3HttpResponse> doGet(String url) throws Exception {
        observe(httpClient.get({ RequestSpec request ->
            request.headers.add('Accept-Charset', 'UTF-8')
            request.url.set(url.toURI())
        })).map { ReceivedResponse resp ->
            new Oct3HttpResponse(statusCode: resp.statusCode, bodyAsBytes: resp.body.getBytes())
        }
    }

    @Override
    rx.Observable<Oct3HttpResponse> doPost(String url, byte[] byteArray) throws Exception {
        URI uri
        observe(httpClient.post({ RequestSpec request ->
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
        })).map { ReceivedResponse resp ->
            new Oct3HttpResponse(uri: uri, statusCode: resp.statusCode, bodyAsBytes: resp.body.getBytes())
        }
    }

    @Override
    rx.Observable<Oct3HttpResponse> doPost(String url, InputStream inputStream) throws Exception {
        doPost(url, IOUtils.toByteArray(inputStream))
    }

}
