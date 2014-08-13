package com.sony.ebs.octopus3.commons.ratpack.product.enhancer

import com.sony.ebs.octopus3.commons.ratpack.http.ning.MockNingResponse
import com.sony.ebs.octopus3.commons.ratpack.http.ning.NingHttpClient
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
class EanCodeEnhancerTest {

    EanCodeEnhancer eanCodeEnhancer
    StubFor mockNingHttpClient
    static ExecController execController

    final static String FEED_WITH_EANCODE = '<eancodes><eancode material="a" code="4905524328974"/></eancodes>'
    final static String FEED_NO_EANCODE = '<eancodes></eancodes>'

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
        eanCodeEnhancer = new EanCodeEnhancer(execControl: execController.control,
                serviceUrl: "/eancode/:product")
        mockNingHttpClient = new StubFor(NingHttpClient)
    }

    def runEnhance() {
        eanCodeEnhancer.httpClient = mockNingHttpClient.proxyInstance()

        def result = new BlockingVariable(5)
        boolean valueSet = false
        execController.start {
            eanCodeEnhancer.enhance([sku: "a"]).subscribe({
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
    void "enhance with eancode"() {
        mockNingHttpClient.demand.with {
            doGet(1) { String url ->
                assert url == "/eancode/A"
                rx.Observable.just(new MockNingResponse(_statusCode: 200, _responseBody: FEED_WITH_EANCODE))
            }
        }
        assert runEnhance() == [sku: "a", eanCode: "4905524328974"]
    }

    @Test
    void "enhance no eancode in feed"() {
        mockNingHttpClient.demand.with {
            doGet(1) { String url ->
                assert url == "/eancode/A"
                rx.Observable.just(new MockNingResponse(_statusCode: 200, _responseBody: FEED_NO_EANCODE))
            }
        }
        assert runEnhance() == [sku: "a"]
    }


    @Test
    void "enhance not found from web service"() {
        mockNingHttpClient.demand.with {
            doGet(1) { String url ->
                assert url == "/eancode/A"
                rx.Observable.just(new MockNingResponse(_statusCode: 404))
            }
        }
        assert runEnhance() == [sku: "a"]
    }
}
