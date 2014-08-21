package com.sony.ebs.octopus3.commons.ratpack.http.ning

import com.ning.http.client.AsyncCompletionHandlerBase
import com.ning.http.client.AsyncHttpClient
import com.ning.http.client.AsyncHttpClient.BoundRequestBuilder
import com.ning.http.client.AsyncHttpClientConfig
import com.ning.http.client.ProxyServer
import com.ning.http.client.Realm
import com.ning.http.client.Response
import groovy.util.logging.Slf4j
import org.apache.http.client.utils.URIBuilder
import ratpack.launch.LaunchConfig
import ratpack.rx.internal.ExecControllerBackedScheduler
import rx.Scheduler

import static ratpack.rx.RxRatpack.observe

@Slf4j
class NingHttpClient {

    enum RequestType {
        GET, POST, DELETE
    }

    LaunchConfig launchConfig
    String authenticationUser, authenticationPassword
    AsyncHttpClient asyncHttpClient
    Scheduler resumingScheduler

    public NingHttpClient() {
    }

    public NingHttpClient(LaunchConfig launchConfig, String proxyHost, int proxyPort,
                          String proxyUser, String proxyPassword, String nonProxyHosts,
                          String authenticationUser, String authenticationPassword,
                          int connectionTimeout, int readTimeout) {
        AsyncHttpClientConfig config
        if (proxyHost) {
            def proxyServer = new ProxyServer(proxyHost, proxyPort, proxyUser, proxyPassword)
            if (nonProxyHosts) {
                nonProxyHosts.split(",")?.collect({ it.trim() })?.each {
                    proxyServer.addNonProxyHost(it)
                }
            }
            config = new AsyncHttpClientConfig.Builder()
                    .setProxyServer(proxyServer)
                    .setConnectionTimeoutInMs(connectionTimeout)
                    .setRequestTimeoutInMs(readTimeout)
                    .build()
        } else {
            config = new AsyncHttpClientConfig.Builder()
                    .setConnectionTimeoutInMs(connectionTimeout)
                    .setRequestTimeoutInMs(readTimeout)
                    .build()
        }
        asyncHttpClient = new AsyncHttpClient(config)

        this.authenticationUser = authenticationUser
        this.authenticationPassword = authenticationPassword

        this.launchConfig = launchConfig

        resumingScheduler = new ExecControllerBackedScheduler(launchConfig.execController)
    }

    public NingHttpClient(LaunchConfig launchConfig) {
        asyncHttpClient = new AsyncHttpClient(new AsyncHttpClientConfig.Builder().build())
        this.launchConfig = launchConfig

        resumingScheduler = new ExecControllerBackedScheduler(launchConfig.execController)
    }

    BoundRequestBuilder createRequestBuilder(RequestType requestType, String urlString, data) {
        def url = new URIBuilder(urlString).toString()

        log.info "starting $requestType $url"

        Realm realm = authenticationUser ? (new Realm.RealmBuilder()).setScheme(Realm.AuthScheme.BASIC).setPrincipal(authenticationUser).setPassword(authenticationPassword).build() : null

        BoundRequestBuilder requestBuilder
        if (RequestType.GET == requestType) {
            requestBuilder = asyncHttpClient.prepareGet(url).addHeader('Accept-Charset', 'UTF-8').setRealm(realm)
        } else if (RequestType.DELETE == requestType) {
            requestBuilder = asyncHttpClient.prepareDelete(url).addHeader('Accept-Charset', 'UTF-8').setRealm(realm)
        } else {
            requestBuilder = asyncHttpClient.preparePost(url)
                    .addHeader('Accept-Charset', 'UTF-8')
                    .addHeader('Content-Type', 'multipart/form-data')
                    .setRealm(realm).setBody(data)
        }
        requestBuilder
    }

    rx.Observable<Response> getResultAsResponse(RequestType requestType, String url, data) {
        observe(launchConfig.execController.control.promise { f ->
            createRequestBuilder(requestType, url, data).execute(new AsyncCompletionHandlerBase() {
                @Override
                Response onCompleted(Response response) throws Exception {
                    f.success(response)
                    log.info "HTTP $response.statusCode $requestType $url"
                    response
                }

                @Override
                void onThrowable(Throwable t) {
                    log.error "HTTP Error $requestType $url", t
                    f.error(t)
                }
            })
        }).observeOn(resumingScheduler)
    }

    rx.Observable<Response> doGet(String url) throws Exception {
        getResultAsResponse(RequestType.GET, url, null)
    }

    rx.Observable<Response> doPost(String url, String data) throws Exception {
        getResultAsResponse(RequestType.POST, url, data)
    }

    rx.Observable<Response> doPost(String url, InputStream inputStream) throws Exception {
        getResultAsResponse(RequestType.POST, url, inputStream)
    }

    rx.Observable<Response> doDelete(String url) throws Exception {
        getResultAsResponse(RequestType.DELETE, url, null)
    }

    public static boolean isSuccess(Response response) {
        return response?.statusCode >= 200 && response?.statusCode < 300
    }

    public static boolean isSuccess(Response response, String message, List errors) {
        boolean success = isSuccess(response)
        if (!success) {
            errors << "HTTP $response.statusCode error $message".toString()
        }
        return success
    }

    public static boolean isSuccess(Response response, String message) {
        return isSuccess(response)
    }


}
