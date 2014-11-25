package com.sony.ebs.octopus3.commons.ratpack.product.filtering

import com.sony.ebs.octopus3.commons.flows.RepoValue
import com.sony.ebs.octopus3.commons.ratpack.encoding.EncodingUtil
import com.sony.ebs.octopus3.commons.ratpack.http.Oct3HttpClient
import com.sony.ebs.octopus3.commons.ratpack.http.Oct3HttpResponse
import com.sony.ebs.octopus3.commons.ratpack.product.cadc.delta.model.DeltaResult
import com.sony.ebs.octopus3.commons.ratpack.product.cadc.delta.model.RepoDelta
import groovy.mock.interceptor.StubFor
import groovy.util.logging.Slf4j
import org.apache.commons.io.IOUtils
import org.junit.AfterClass
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import org.springframework.core.io.DefaultResourceLoader
import ratpack.exec.ExecController
import ratpack.launch.LaunchConfigBuilder
import spock.util.concurrent.BlockingVariable

@Slf4j
class CategoryServiceTest {

    DefaultResourceLoader defaultResourceLoader = new DefaultResourceLoader()
    final static String BASE_PATH = "classpath:com/sony/ebs/octopus3/commons/ratpack/product/filtering/"

    final static String CATEGORY_FEED = "<categories/>"

    CategoryService categoryService
    StubFor mockHttpClient
    RepoDelta delta
    DeltaResult deltaResult

    static ExecController execController

    def getFileText(name) {
        IOUtils.toString(defaultResourceLoader.getResource(BASE_PATH + name)?.inputStream, EncodingUtil.CHARSET)
    }

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
        categoryService = new CategoryService(
                execControl: execController.control,
                octopusCategoryServiceUrl: "/product/publications/:publication/locales/:locale/hierarchies/category",
                repositoryFileServiceUrl: "/repository/file/:urn")
        mockHttpClient = new StubFor(Oct3HttpClient)
        delta = new RepoDelta(type: RepoValue.flixMedia, publication: "score", locale: "en_gb")
        deltaResult = new DeltaResult()
    }

    def runSaveCategoryFeed(String categoryFeed) {
        categoryService.httpClient = mockHttpClient.proxyInstance()

        def result = new BlockingVariable<String>(5)
        boolean valueSet = false
        execController.start {
            categoryService.saveCategoryFeed(delta, categoryFeed, deltaResult.errors).subscribe({
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
    void "test saveCategoryFeed"() {
        def categoryFeed = getFileText("category_ru.xml")
        mockHttpClient.demand.with {
            doPost(1) { String url, InputStream is ->
                assert url == "/repository/file/urn:flixmedia:score:en_gb:category.xml"
                assert IOUtils.toString(is, EncodingUtil.CHARSET) == categoryFeed
                rx.Observable.just(new Oct3HttpResponse(statusCode: 200))
            }
        }
        assert runSaveCategoryFeed(categoryFeed) == categoryFeed
    }

    @Test
    void "test saveCategoryFeed could not save"() {
        def categoryFeed = getFileText("category_ru.xml")
        mockHttpClient.demand.with {
            doPost(1) { String url, InputStream is ->
                assert url == "/repository/file/urn:flixmedia:score:en_gb:category.xml"
                assert IOUtils.toString(is, EncodingUtil.CHARSET) == categoryFeed
                rx.Observable.just(new Oct3HttpResponse(statusCode: 404))
            }
        }
        categoryService.httpClient = mockHttpClient.proxyInstance()

        assert runSaveCategoryFeed(categoryFeed) == "outOfFlow"
        assert deltaResult.errors == ["HTTP 404 error saving octopus category feed"]
    }

    @Test
    void "test saveCategoryFeed exception in save"() {
        def categoryFeed = getFileText("category_ru.xml")
        mockHttpClient.demand.with {
            doPost(1) { String url, InputStream is ->
                assert url == "/repository/file/urn:flixmedia:score:en_gb:category.xml"
                assert IOUtils.toString(is, EncodingUtil.CHARSET) == categoryFeed
                throw new Exception("exception in save")
            }
        }
        categoryService.httpClient = mockHttpClient.proxyInstance()

        assert runSaveCategoryFeed(categoryFeed) == "error"
    }

    def runRetrieveCategoryFeed(String publication, String locale, List errors) {
        categoryService.httpClient = mockHttpClient.proxyInstance()

        def result = new BlockingVariable<String>(5)
        boolean valueSet = false
        execController.start {
            categoryService.retrieveCategoryFeed(publication, locale, errors).subscribe({
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
    void "test retrieveCategoryFeed"() {
        def categoryFeed = getFileText("category_ru.xml")

        mockHttpClient.demand.with {
            doGet(1) {
                assert it == "/product/publications/SCORE/locales/en_GB/hierarchies/category"
                rx.Observable.just(new Oct3HttpResponse(statusCode: 200, bodyAsBytes: categoryFeed.bytes))
            }
        }
        categoryService.httpClient = mockHttpClient.proxyInstance()

        List errors = []
        assert runRetrieveCategoryFeed("score", "en_gb", errors) == categoryFeed
        assert !errors
    }

    @Test
    void "test retrieveCategoryFeed category not found"() {
        mockHttpClient.demand.with {
            doGet(1) {
                assert it == "/product/publications/SCORE/locales/en_GB/hierarchies/category"
                rx.Observable.just(new Oct3HttpResponse(statusCode: 500))
            }
        }
        categoryService.httpClient = mockHttpClient.proxyInstance()

        List errors = []
        assert runRetrieveCategoryFeed("score", "en_gb", errors) == "outOfFlow"
        assert errors == ["HTTP 500 error getting octopus category feed"]
    }

    @Test
    void "test retrieveCategoryFeed exception in get"() {
        mockHttpClient.demand.with {
            doGet(1) {
                assert it == "/product/publications/SCORE/locales/en_GB/hierarchies/category"
                throw new Exception("exception in get")
            }
        }
        categoryService.httpClient = mockHttpClient.proxyInstance()

        List errors = []
        assert runRetrieveCategoryFeed("score", "en_gb", errors) == "error"
        assert !errors
    }

    def runFilterForCategory(List productUrls, String categoryFeed) {
        def result = new BlockingVariable<List>(5)
        boolean valueSet = false
        execController.start {
            categoryService.filterForCategory(productUrls, categoryFeed).subscribe({
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
    void "test filterForCategory"() {
        def xml = """
<ProductHierarchy name="category" publication="SCORE" locale="en_GB">
    <node>
        <name><![CDATA[SCORE]]></name>
        <displayName><![CDATA[SCORE]]></displayName>
        <nodes>
            <node>
                <name><![CDATA[playstation]]></name>
                <displayName><![CDATA[playstation new]]></displayName>
                <nodes>
                    <node>
                        <name><![CDATA[psvita]]></name>
                        <displayName><![CDATA[psvita new]]></displayName>
                        <products>
                            <product><![CDATA[A1]]></product>
                            <product><![CDATA[A2]]></product>
                            <product><![CDATA[SS-AC3+/C CE7]]></product>
                        </products>
                    </node>
                    <node>
                        <name><![CDATA[ps4]]></name>
                        <displayName><![CDATA[ps4 new]]></displayName>
                        <products>
                            <product><![CDATA[C1]]></product>
                            <product><![CDATA[C2]]></product>
                            <product><![CDATA[SS-AC3//C CE7]]></product>
                        </products>
                    </node>
                </nodes>
            </node>
        </nodes>
    </node>
</ProductHierarchy>
"""
        def productUrls = [
                "urn:test_sku:score:en_gb:a1",
                "urn:test_sku:score:en_gb:b",
                "urn:test_sku:score:en_gb:c2",
                "urn:test_sku:score:en_gb:d",
                "urn:test_sku:score:en_gb:ss-ac3_2f_2fc+ce7",
                "urn:test_sku:score:en_gb:ss-ac3_2b_2fc+ce7"
        ]

        Map filtered = runFilterForCategory(productUrls, xml)
        assert filtered == [
                "urn:test_sku:score:en_gb:a1": "psvita",
                "urn:test_sku:score:en_gb:c2": "ps4",
                "urn:test_sku:score:en_gb:ss-ac3_2b_2fc+ce7": "psvita",
                "urn:test_sku:score:en_gb:ss-ac3_2f_2fc+ce7": "ps4"
        ]
    }

}
