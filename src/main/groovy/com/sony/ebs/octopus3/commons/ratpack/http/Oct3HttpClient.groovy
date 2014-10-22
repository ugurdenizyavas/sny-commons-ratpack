package com.sony.ebs.octopus3.commons.ratpack.http

interface Oct3HttpClient {

    enum HttpMethod {
        GET, POST, DELETE
    }

    rx.Observable<Oct3HttpResponse> doGet(String url) throws Exception

    rx.Observable<Oct3HttpResponse> doPost(String url, byte[] byteArray) throws Exception

    rx.Observable<Oct3HttpResponse> doPost(String url, InputStream inputStream) throws Exception

    rx.Observable<Oct3HttpResponse> doPost(String url, String text) throws Exception

    rx.Observable<Oct3HttpResponse> doDelete(String url) throws Exception

}
