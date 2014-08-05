package com.sony.ebs.octopus3.commons.ratpack.http.ning

import com.ning.http.client.FluentCaseInsensitiveStringsMap
import com.ning.http.client.Response
import com.ning.http.client.cookie.Cookie

import java.nio.ByteBuffer

class MockNingResponse implements Response{

    String _responseBody
    int _statusCode

    @Override
    int getStatusCode() {
        return _statusCode
    }

    @Override
    String getStatusText() {
        return null
    }

    @Override
    byte[] getResponseBodyAsBytes() throws IOException {
        return new byte[0]
    }

    @Override
    ByteBuffer getResponseBodyAsByteBuffer() throws IOException {
        return null
    }

    @Override
    InputStream getResponseBodyAsStream() throws IOException {
        return null
    }

    @Override
    String getResponseBodyExcerpt(int maxLength, String charset) throws IOException {
        return null
    }

    @Override
    String getResponseBody(String charset) throws IOException {
        return _responseBody
    }

    @Override
    String getResponseBodyExcerpt(int maxLength) throws IOException {
        return null
    }

    @Override
    String getResponseBody() throws IOException {
        return _responseBody
    }

    @Override
    URI getUri() throws MalformedURLException {
        return null
    }

    @Override
    String getContentType() {
        return null
    }

    @Override
    String getHeader(String name) {
        return null
    }

    @Override
    List<String> getHeaders(String name) {
        return null
    }

    @Override
    FluentCaseInsensitiveStringsMap getHeaders() {
        return null
    }

    @Override
    boolean isRedirected() {
        return false
    }

    @Override
    List<Cookie> getCookies() {
        return null
    }

    @Override
    boolean hasResponseStatus() {
        return false
    }

    @Override
    boolean hasResponseHeaders() {
        return false
    }

    @Override
    boolean hasResponseBody() {
        return false
    }
}
