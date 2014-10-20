package com.sony.ebs.octopus3.commons.ratpack.http

class Oct3HttpResponse {

    byte[] bodyAsBytes
    int statusCode
    URI uri

    boolean isSuccess() {
        statusCode >= 200 && statusCode < 300
    }

    InputStream getBodyAsStream() {
        new ByteArrayInputStream(bodyAsBytes)
    }

}
