package com.sony.ebs.octopus3.commons.ratpack.http

import org.junit.Test

class Oct3HttpResponseTest {

    @Test
    void "test isSuccessful with error"() {
        List errors = []
        new Oct3HttpResponse(statusCode: 501).isSuccessful("doing sth", errors)
        assert errors == ["HTTP 501 error doing sth"]
    }

    @Test
    void "test isSuccessful"() {
        List errors = []
        new Oct3HttpResponse(statusCode: 201).isSuccessful("doing sth", errors)
        assert errors == []
    }
}