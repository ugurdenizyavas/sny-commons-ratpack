package com.sony.ebs.octopus3.commons.ratpack.http

import com.sony.ebs.octopus3.commons.ratpack.http.ning.NingOct3HttpClient
import com.sony.ebs.octopus3.commons.ratpack.http.ratpack.RatpackOct3HttpClient
import groovy.util.logging.Slf4j
import ratpack.launch.LaunchConfig

@Slf4j
class Oct3HttpClientFactory {

    enum HttpClientType {
        ning,
        ratpack
    }

    Oct3HttpClient createHttpClient(LaunchConfig launchConfig, String httpClientType,
                                    String proxyHost, int proxyPort,
                                    String proxyUser, String proxyPassword, String nonProxyHosts,
                                    String authenticationUser, String authenticationPassword,
                                    int connectionTimeout, int readTimeout) {
        def httpClient
        if (httpClientType == HttpClientType.ning.toString()) {
            httpClient = new NingOct3HttpClient(launchConfig, proxyHost, proxyPort, proxyUser, proxyPassword, nonProxyHosts,
                    authenticationUser, authenticationPassword, connectionTimeout, readTimeout)
        } else {
            httpClient = new RatpackOct3HttpClient(launchConfig, authenticationUser, authenticationPassword, connectionTimeout, readTimeout)
        }
        log.info "created Http Client typed {} : {}", httpClientType, httpClient
        httpClient
    }

}
