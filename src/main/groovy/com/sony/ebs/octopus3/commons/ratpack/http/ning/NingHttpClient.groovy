package com.sony.ebs.octopus3.commons.ratpack.http.ning

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
import rx.schedulers.Schedulers

@Slf4j
class NingHttpClient {

    enum RequestType {
        GET, POST, DELETE
    }

    LaunchConfig launchConfig
    String authenticationUser, authenticationPassword
    AsyncHttpClient asyncHttpClient
    Scheduler ratpackScheduler

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

        this.launchConfig = launchConfig
        this.authenticationUser = authenticationUser
        this.authenticationPassword = authenticationPassword

        ratpackScheduler = new ExecControllerBackedScheduler(launchConfig.execController)
    }

    public NingHttpClient(LaunchConfig launchConfig) {
        asyncHttpClient = new AsyncHttpClient(new AsyncHttpClientConfig.Builder().build())
        this.launchConfig = launchConfig
        ratpackScheduler = new ExecControllerBackedScheduler(launchConfig.execController)
    }

    private def executeRequest(RequestType requestType, String urlString, String data = null) {
        def url = new URIBuilder(urlString).toString()

        log.info "starting $requestType for $url"

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
        requestBuilder.execute()
    }

    public static boolean isSuccess(Response response) {
        return response.statusCode >= 200 && response.statusCode < 300
    }

    rx.Observable<Response> getResultAsResponse(RequestType requestType, String url, String data = null)
            throws Exception {
        rx.Observable.from(executeRequest(requestType, url, data), Schedulers.io()).observeOn(ratpackScheduler)
    }

    rx.Observable<Response> doGet(String url) throws Exception {
        getResultAsResponse(RequestType.GET, url)
    }

    rx.Observable<Response> doPost(String url, String data) throws Exception {
        getResultAsResponse(RequestType.POST, url, data)
    }

    rx.Observable<Response> doDelete(String url) throws Exception {
        getResultAsResponse(RequestType.DELETE, url)
    }

}
