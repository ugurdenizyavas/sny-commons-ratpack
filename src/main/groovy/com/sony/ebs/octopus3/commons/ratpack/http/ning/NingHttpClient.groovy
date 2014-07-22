package com.sony.ebs.octopus3.commons.ratpack.http.ning

import com.ning.http.client.AsyncHttpClient
import com.ning.http.client.AsyncHttpClient.BoundRequestBuilder
import com.ning.http.client.AsyncHttpClientConfig
import com.ning.http.client.ProxyServer
import com.ning.http.client.Realm
import groovy.util.logging.Slf4j
import org.apache.http.client.utils.URIBuilder
import ratpack.exec.ExecControl

@Slf4j
class NingHttpClient {

    enum RequestType {
        GET, POST, DELETE
    }

    ExecControl execControl
    String authenticationUser, authenticationPassword
    AsyncHttpClient asyncHttpClient

    public NingHttpClient() {
    }

    public NingHttpClient(ExecControl execControl, String proxyHost, int proxyPort,
                          String proxyUser, String proxyPassword, String nonProxyHosts,
                          String authenticationUser, String authenticationPassword) {
        AsyncHttpClientConfig config
        if (proxyHost) {
            def proxyServer = new ProxyServer(proxyHost, proxyPort, proxyUser, proxyPassword)
            if (nonProxyHosts) {
                nonProxyHosts.split(",")?.collect { it.trim() }.each {
                    proxyServer.addNonProxyHost(it)
                }
            }
            config = new AsyncHttpClientConfig.Builder().setProxyServer(proxyServer).build()
        } else {
            config = new AsyncHttpClientConfig.Builder().build()
        }
        asyncHttpClient = new AsyncHttpClient(config)

        this.execControl = execControl
        this.authenticationUser = authenticationUser
        this.authenticationPassword = authenticationPassword
    }

    public NingHttpClient(ExecControl execControl) {
        asyncHttpClient = new AsyncHttpClient(new AsyncHttpClientConfig.Builder().build())
        this.execControl = execControl
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

    rx.Observable<String> executeRequestObservable(RequestType requestType, String url, String data = null)
            throws Exception {
        rx.Observable.from(executeRequest(requestType, url, data)).map({ response ->
            if (response.statusCode < 200 || response.statusCode > 299) {
                def message = "error getting $response.uri with http status code $response.statusCode"
                log.error message
                throw new Exception(message)
            }
            log.error "finished getting $response.uri with http status code $response.statusCode"
            response.responseBody
        })
    }

    rx.Observable<String> doGet(String url) throws Exception {
        executeRequestObservable(RequestType.GET, url)
    }

    rx.Observable<String> doPost(String url, String data) throws Exception {
        executeRequestObservable(RequestType.POST, url, data)
    }

    rx.Observable<String> doDelete(String url) throws Exception {
        executeRequestObservable(RequestType.DELETE, url)
    }

}
