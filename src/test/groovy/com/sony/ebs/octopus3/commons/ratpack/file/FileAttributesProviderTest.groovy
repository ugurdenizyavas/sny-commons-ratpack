package com.sony.ebs.octopus3.commons.ratpack.file

import com.sony.ebs.octopus3.commons.ratpack.http.Oct3HttpClient
import com.sony.ebs.octopus3.commons.ratpack.http.Oct3HttpResponse
import com.sony.ebs.octopus3.commons.urn.URNImpl
import groovy.mock.interceptor.StubFor
import groovy.util.logging.Slf4j
import org.junit.AfterClass
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import ratpack.exec.ExecController
import ratpack.launch.LaunchConfigBuilder
import spock.util.concurrent.BlockingVariable

@Slf4j
class FileAttributesProviderTest {

    final static String FILE_ATTR_FEED = '''
{
    "status": 200,
    "result": {
        "lastModifiedTime": "t1",
        "lastAccessTime": "t2",
        "creationTime": "2014-08-08T08:18:27.000+02:00",
        "regularFile": false,
        "directory": true,
        "size": 60416
    }
}
'''

    FileAttributesProvider deltaDatesProvider
    StubFor mockHttpClient

    static ExecController execController

    @BeforeClass
    static void beforeClass() {
        execController = LaunchConfigBuilder.noBaseDir().build().execController
    }

    @AfterClass
    static void afterClass() {
        if (execController) execController.close()
    }

    @Before
    void before() {
        deltaDatesProvider = new FileAttributesProvider(
                execControl: execController.control,
                repositoryFileAttributesServiceUrl: "/repository/fileattributes/:urn")
        mockHttpClient = new StubFor(Oct3HttpClient)
    }

    def runGetLastModifiedTime() {
        deltaDatesProvider.httpClient = mockHttpClient.proxyInstance()

        def result = new BlockingVariable(5)
        boolean valueSet = false
        execController.start {
            deltaDatesProvider.getLastModifiedTime(new URNImpl("urn:test:a")).subscribe({
                valueSet = true
                result.set(it)
            }, {
                log.error "error", it
                result.set("error")
            }, {
                if (!valueSet) result.set("outOfFlow")
            })
        }
        result.get()
    }

    @Test
    void "getLastModifiedTime found"() {
        mockHttpClient.demand.with {
            doGet(1) { String url ->
                assert url == "/repository/fileattributes/urn:test:a"
                rx.Observable.just(new Oct3HttpResponse(statusCode: 200, bodyAsBytes: FILE_ATTR_FEED.getBytes("UTF-8")))
            }
        }
        runGetLastModifiedTime() == new FileAttribute(found: true, value: "t1")
    }

    @Test
    void "getLastModifiedTime not found"() {
        mockHttpClient.demand.with {
            doGet(1) { String url ->
                assert url == "/repository/fileattributes/urn:test:a"
                rx.Observable.just(new Oct3HttpResponse(statusCode: 404))
            }
        }
        runGetLastModifiedTime() == new FileAttribute(found: false)
    }

    @Test
    void "getLastModifiedTime error"() {
        mockHttpClient.demand.with {
            doGet(1) { String url ->
                throw new Exception("error getLastModifiedTime")
            }
        }
        assert runGetLastModifiedTime() == "error"
    }
}
