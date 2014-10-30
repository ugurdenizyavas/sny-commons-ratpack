package com.sony.ebs.octopus3.commons.ratpack.http

import com.ning.http.client.Response
import com.sony.ebs.octopus3.commons.ratpack.encoding.EncodingUtil

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

    String getBodyAsText() {
        new String(bodyAsBytes, EncodingUtil.CHARSET)
    }

    public boolean isSuccessful(String message, List errors) {
        if (!success) {
            errors << "HTTP $statusCode error $message".toString()
        }
        return success
    }

}
