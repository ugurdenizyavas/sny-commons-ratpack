package com.sony.ebs.octopus3.commons.ratpack.product.cadc.delta.service

import com.ning.http.client.Response
import com.sony.ebs.octopus3.commons.process.ProcessIdImpl
import com.sony.ebs.octopus3.commons.ratpack.http.ning.MockNingResponse
import com.sony.ebs.octopus3.commons.ratpack.http.ning.NingHttpClient
import com.sony.ebs.octopus3.commons.ratpack.product.cadc.delta.model.Delta
import com.sony.ebs.octopus3.commons.ratpack.product.cadc.delta.model.DeltaType
import groovy.mock.interceptor.MockFor
import groovy.mock.interceptor.StubFor
import groovy.util.logging.Slf4j
import org.apache.http.client.utils.URIBuilder
import org.junit.AfterClass
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import ratpack.exec.ExecController
import ratpack.launch.LaunchConfigBuilder
import spock.util.concurrent.BlockingVariable

@Slf4j
class AbstractDeltaServiceTest {

    AbstractDeltaService deltaService
    StubFor mockDeltaUrlHelper
    MockFor mockHttpClient

    Delta delta

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
        mockDeltaUrlHelper = new StubFor(DeltaUrlHelper)
        mockHttpClient = new MockFor(NingHttpClient)

        delta = new Delta(type: DeltaType.global_sku, publication: "SCORE", locale: "en_GB", since: "2014", cadcUrl: "http://cadc")

        deltaService = new AbstractDeltaService() {
            @Override
            Object createServiceResult(Response response, String cadcUrl) {
                return [body: response.responseBody, cadcUrl: cadcUrl, statusCode: response.statusCode]
            }

            @Override
            Object createServiceResultOnError(String error, String cadcUrl) {
                return [error: error, cadcUrl: cadcUrl]
            }
        }
        deltaService.with {
            execControl = execController.control
            cadcsourceSheetServiceUrl = "http://cadcsource/sheet/publication/:publication/locale/:locale"
        }
    }

    List runFlow() {
        def mockHttpClientPI = mockHttpClient.proxyInstance()
        deltaService.localHttpClient = mockHttpClientPI
        deltaService.cadcHttpClient = mockHttpClientPI
        deltaService.deltaUrlHelper = mockDeltaUrlHelper.proxyInstance()

        def result = new BlockingVariable(5)
        execController.start {
            deltaService.deltaFlow(delta).toList().subscribe({
                result.set(it)
            }, {
                log.error "error in flow", it
                result.set([it.message])
            })
        }
        result.get()
    }

    def createDeltaResponse() {
        """
        {
            "skus" : {
                "en_GB" : [
                    "http://cadc/a",
                    "http://cadc/c",
                    "http://cadc/b"
                ]
            }
        }
        """
    }

    def createSheetResponse(sku) {
        "sheet $sku"
    }

    def getSkuFromUrl(url) {
        def importUrl = new URIBuilder(url).queryParams[0].value
        def sku = importUrl.substring(importUrl.size() - 1)
        sku
    }

    @Test
    void "success"() {
        mockDeltaUrlHelper.demand.with {
            createSinceValue(1) { since, urn ->
                assert since == delta.since
                assert urn == delta.lastModifiedUrn
                rx.Observable.just("s1")
            }
            createCadcDeltaUrl(1) { cadcUrl, locale, since ->
                assert cadcUrl == "http://cadc"
                assert locale == "en_GB"
                assert since == "s1"
                rx.Observable.just("http://cadc/delta")
            }
            updateLastModified(1) { lastModifiedUrn, errors ->
                rx.Observable.just("done")
            }
        }

        mockHttpClient.demand.with {
            doGet(1) { String url ->
                assert url == "http://cadc/delta"
                rx.Observable.from(new MockNingResponse(_statusCode: 200, _responseBody: createDeltaResponse()))
            }
            doGet(3) { String url ->
                def sku = getSkuFromUrl(url)
                assert url == "http://cadcsource/sheet/publication/SCORE/locale/en_GB?url=http%3A%2F%2Fcadc%2F$sku&processId=123"
                rx.Observable.from(new MockNingResponse(_statusCode: 200, _responseBody: createSheetResponse(sku)))
            }
        }

        delta.processId = new ProcessIdImpl("123")
        List result = runFlow().sort()
        assert result.size() == 3

        assert result.contains([body: "sheet a", cadcUrl: "http://cadc/a", statusCode: 200])
        assert result.contains([body: "sheet b", cadcUrl: "http://cadc/b", statusCode: 200])
        assert result.contains([body: "sheet c", cadcUrl: "http://cadc/c", statusCode: 200])

        assert delta.finalCadcUrl == "http://cadc/delta"
        assert delta.finalSince == "s1"
    }

    @Test
    void "no products to import"() {
        mockDeltaUrlHelper.demand.with {
            createSinceValue(1) { since, urn ->
                rx.Observable.just("s1")
            }
            createCadcDeltaUrl(1) { cadcUrl, locale, since ->
                rx.Observable.just("http://cadc/delta")
            }
            updateLastModified(1) { lastModifiedUrn, errors ->
                rx.Observable.just("done")
            }
        }
        mockHttpClient.demand.with {
            doGet(1) { String url ->
                rx.Observable.from(new MockNingResponse(_statusCode: 200, _responseBody: '{"skus":{"en_GB":[]}}'))
            }
        }
        def result = runFlow()
        assert result.size() == 0
        assert delta.finalCadcUrl == "http://cadc/delta"
    }

    @Test
    void "error getting delta"() {
        mockDeltaUrlHelper.demand.with {
            createSinceValue(1) { since, urn ->
                rx.Observable.just("s1")
            }
            createCadcDeltaUrl(1) { cadcUrl, locale, since ->
                rx.Observable.just("http://cadc/delta")
            }
        }
        mockHttpClient.demand.with {
            doGet(1) {
                rx.Observable.from(new MockNingResponse(_statusCode: 500))
            }
        }
        def result = runFlow()
        assert result.size() == 0
        assert delta.errors == ["HTTP 500 error getting delta json from cadc"]
    }

    @Test
    void "error parsing cadc delta json"() {
        mockDeltaUrlHelper.demand.with {
            createSinceValue(1) { since, urn ->
                rx.Observable.just("s1")
            }
            createCadcDeltaUrl(1) { cadcUrl, locale, since ->
                rx.Observable.just("http://cadc/delta")
            }
        }
        mockHttpClient.demand.with {
            doGet(1) { String url ->
                assert url == "http://cadc/delta"
                rx.Observable.from(new MockNingResponse(_statusCode: 200, _responseBody: 'invalid json'))
            }
        }
        def result = runFlow()
        assert result == ["error parsing cadc delta json"]
    }

    @Test
    void "error updating last modified date"() {
        mockDeltaUrlHelper.demand.with {
            createSinceValue(1) { since, urn ->
                rx.Observable.just("s1")
            }
            createCadcDeltaUrl(1) { cadcUrl, locale, since ->
                rx.Observable.just("http://cadc/delta")
            }
            updateLastModified(1) { lastModifiedUrn, errors ->
                rx.Observable.just("error").filter({
                    delta.errors << "error updating last modified date"
                    false
                })
            }
        }
        mockHttpClient.demand.with {
            doGet(1) { String url ->
                assert url == "http://cadc/delta"
                rx.Observable.from(new MockNingResponse(_statusCode: 200, _responseBody: '{"skus":{"en_GB":["http://cadc/a", "http://cadc/c", "http://cadc/b"]}}'))
            }
        }
        def result = runFlow()
        assert result.size() == 0
        assert delta.errors == ["error updating last modified date"]
    }

    @Test
    void "one sheet is not imported"() {
        mockDeltaUrlHelper.demand.with {
            createSinceValue(1) { since, urn ->
                rx.Observable.just("s1")
            }
            createCadcDeltaUrl(1) { cadcUrl, locale, since ->
                rx.Observable.just("http://cadc/delta")
            }
            updateLastModified(1) { lastModifiedUrn, errors ->
                rx.Observable.just("done")
            }
        }

        mockHttpClient.demand.with {
            doGet(1) { String url ->
                assert url == "http://cadc/delta"
                rx.Observable.from(new MockNingResponse(_statusCode: 200, _responseBody: '{"skus":{"en_GB":["http://cadc/a", "http://cadc/c", "http://cadc/b"]}}'))
            }
            doGet(3) { String url ->
                def sku = getSkuFromUrl(url)
                assert url == "http://cadcsource/sheet/publication/SCORE/locale/en_GB?url=http%3A%2F%2Fcadc%2F$sku"
                if (sku == "b") {
                    rx.Observable.from(new MockNingResponse(_statusCode: 500, _responseBody: 'err1'))
                } else {
                    rx.Observable.from(new MockNingResponse(_statusCode: 200, _responseBody: createSheetResponse(sku)))
                }
            }
        }
        def result = runFlow().sort()
        assert result.size() == 3
        assert result.contains([body: "sheet a", cadcUrl: "http://cadc/a", statusCode: 200])
        assert result.contains([body: "err1", cadcUrl: "http://cadc/b", statusCode: 500])
        assert result.contains([body: "sheet c", cadcUrl: "http://cadc/c", statusCode: 200])

        assert delta.finalCadcUrl == "http://cadc/delta"
        assert delta.finalSince == "s1"
    }

}
