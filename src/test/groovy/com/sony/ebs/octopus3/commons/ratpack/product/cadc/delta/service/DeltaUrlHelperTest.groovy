package com.sony.ebs.octopus3.commons.ratpack.product.cadc.delta.service

import com.sony.ebs.octopus3.commons.flows.RepoValue
import com.sony.ebs.octopus3.commons.ratpack.file.FileAttribute
import com.sony.ebs.octopus3.commons.ratpack.file.FileAttributesProvider
import com.sony.ebs.octopus3.commons.ratpack.http.Oct3HttpClient
import com.sony.ebs.octopus3.commons.ratpack.http.Oct3HttpResponse
import com.sony.ebs.octopus3.commons.ratpack.product.cadc.delta.model.CadcDelta
import com.sony.ebs.octopus3.commons.ratpack.product.cadc.delta.model.DeltaResult
import com.sony.ebs.octopus3.commons.urn.URN
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
class DeltaUrlHelperTest {

    DeltaUrlHelper deltaUrlHelper

    StubFor mockHttpClient, mockFileAttributesProvider
    DeltaResult deltaResult

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
        deltaUrlHelper = new DeltaUrlHelper()
        deltaUrlHelper = new DeltaUrlHelper(
                execControl: execController.control,
                repositoryFileServiceUrl: "/repository/file/:urn")
        mockHttpClient = new StubFor(Oct3HttpClient)
        mockFileAttributesProvider = new StubFor(FileAttributesProvider)

        deltaResult = new DeltaResult()
    }

    def runUpdateLastModified() {
        def delta = new CadcDelta(type: RepoValue.global_sku, publication: "SCORE", locale: "fr_BE")
        deltaUrlHelper.httpClient = mockHttpClient.proxyInstance()

        def result = new BlockingVariable<String>(5)
        boolean valueSet = false
        execController.start {
            deltaUrlHelper.updateLastModified(delta.lastModifiedUrn, deltaResult.errors).subscribe({
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
    void "update last modified"() {
        mockHttpClient.demand.with {
            doPost(1) { String url, String data ->
                assert url == "/repository/file/urn:global_sku:last_modified:score:fr_be"
                assert data == "update"
                rx.Observable.just(new Oct3HttpResponse(statusCode: 200))
            }
        }
        assert runUpdateLastModified() == "done"
    }

    @Test
    void "update last modified outOfFlow"() {
        mockHttpClient.demand.with {
            doPost(1) { String url, String data ->
                rx.Observable.just(new Oct3HttpResponse(statusCode: 500))
            }
        }
        assert runUpdateLastModified() == "outOfFlow"
        assert deltaResult.errors == ["HTTP 500 error updating last modified date"]
    }

    @Test
    void "update last modified error"() {
        mockHttpClient.demand.with {
            doPost(1) { String url, String data ->
                throw new Exception("error updating last modified time")
            }
        }
        assert runUpdateLastModified() == "error"
    }

    def runCreateSinceValue(String since) {
        deltaUrlHelper.fileAttributesProvider = mockFileAttributesProvider.proxyInstance()

        def delta = new CadcDelta(type: RepoValue.global_sku ,publication: "SCORE", locale: "fr_BE", cadcUrl: "http://cadc", since: since)

        def result = new BlockingVariable<String>(5)
        boolean valueSet = false
        execController.start {
            deltaUrlHelper.createSinceValue(delta.since, delta.lastModifiedUrn).subscribe({
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
    void "create delta with value"() {
        assert runCreateSinceValue("2014-07-17T14:35:25.089+03:00") == "2014-07-17T14:35:25.089+03:00"
    }

    @Test
    void "create since with value all"() {
        assert runCreateSinceValue("All") == "All"
    }

    @Test
    void "create since last modified found"() {
        mockFileAttributesProvider.demand.with {
            getLastModifiedTime(1) { URN urn ->
                assert urn.toString() == "urn:global_sku:last_modified:score:fr_be"
                rx.Observable.just(new FileAttribute(found: true, value: "s1"))
            }
        }
        assert runCreateSinceValue(null) == "s1"
    }

    @Test
    void "create since last modified not found"() {
        mockFileAttributesProvider.demand.with {
            getLastModifiedTime(1) { URN urn ->
                assert urn.toString() == "urn:global_sku:last_modified:score:fr_be"
                rx.Observable.just(new FileAttribute(found: false))
            }
        }
        assert runCreateSinceValue(null) == ""
    }

    def runCreateCadcDeltaUrl(String since) {
        def result = new BlockingVariable<String>(5)
        boolean valueSet = false
        execController.start {
            deltaUrlHelper.createCadcDeltaUrl("http://cadc/delta", "fr_BE", since).subscribe({
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
    void "create cadc delta url with since"() {
        assert runCreateCadcDeltaUrl("s1") == "http://cadc/delta/changes/fr_BE?since=s1"
    }

    @Test
    void "create cadc delta url null since"() {
        assert runCreateCadcDeltaUrl(null) == "http://cadc/delta/fr_BE"
    }

    @Test
    void "create cadc delta url empty since"() {
        assert runCreateCadcDeltaUrl("") == "http://cadc/delta/fr_BE"
    }

    @Test
    void "create cadc delta url encoded since"() {
        assert runCreateCadcDeltaUrl("2014-07-17T14:35:25.089+03:00") == "http://cadc/delta/changes/fr_BE?since=2014-07-17T14%3A35%3A25.089%2B03%3A00"
    }

    def runCreateStartDate(String sdate) {
        deltaUrlHelper.fileAttributesProvider = mockFileAttributesProvider.proxyInstance()

        def result = new BlockingVariable<String>(5)
        boolean valueSet = false
        execController.start {
            deltaUrlHelper.createStartDate(sdate, new URNImpl("a", ["b", "c"])).subscribe({
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
    void "create start date existing"() {
        assert runCreateStartDate("2014-07-17T14:35:25.089+03:00") == "2014-07-17T14:35:25.089+03:00"
    }

    @Test
    void "create start date last modified found"() {
        mockFileAttributesProvider.demand.with {
            getLastModifiedTime(1) { URN urn ->
                assert urn.toString() == "urn:a:b:c"
                rx.Observable.just(new FileAttribute(found: true, value: "2014-07-17T14:35:25.089+03:00"))
            }
        }
        assert runCreateStartDate(null) == "2014-07-17T14:35:25.089+03:00"
    }

    @Test
    void "create start date last modified not found"() {
        mockFileAttributesProvider.demand.with {
            getLastModifiedTime(1) { URN urn ->
                assert urn.toString() == "urn:a:b:c"
                rx.Observable.just(new FileAttribute(found: false))
            }
        }
        assert runCreateStartDate(null) == null
    }

    def runCreateRepoDeltaUrl(String sdate, String edate) {
        def result = new BlockingVariable<String>(5)
        boolean valueSet = false
        execController.start {
            deltaUrlHelper.createRepoDeltaUrl("//delta", sdate, edate).subscribe({
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
    void "create repo delta url"() {
        assert runCreateRepoDeltaUrl("s1", "s2") == "//delta?sdate=s1&edate=s2"
    }

    @Test
    void "create repo delta url for sdate"() {
        assert runCreateRepoDeltaUrl("s1", null) == "//delta?sdate=s1"
    }

    @Test
    void "create repo delta url for edate"() {
        assert runCreateRepoDeltaUrl("", "s2") == "//delta?edate=s2"
    }

    @Test
    void "create repo delta url encoded"() {
        assert runCreateRepoDeltaUrl("2014-07-17T14:35:25.089+03:00", "2013-08-11T01:00:00.100+02:00") == "//delta?sdate=2014-07-17T14%3A35%3A25.089%2B03%3A00&edate=2013-08-11T01%3A00%3A00.100%2B02%3A00"
    }

}
