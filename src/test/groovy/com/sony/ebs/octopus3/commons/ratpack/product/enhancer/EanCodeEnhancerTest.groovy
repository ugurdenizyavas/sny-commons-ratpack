package com.sony.ebs.octopus3.commons.ratpack.product.enhancer

import com.sony.ebs.octopus3.commons.ratpack.http.Oct3HttpClient
import com.sony.ebs.octopus3.commons.ratpack.http.Oct3HttpResponse
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
        eanCodeEnhancer = new EanCodeEnhancer(
                execControl: execController.control,
                serviceUrl: "/eancode/:product")
        mockHttpClient = new StubFor(Oct3HttpClient)
    }

    def runEnhance(boolean encoded, String product, String productKey = "sku") {
        eanCodeEnhancer.httpClient = mockHttpClient.proxyInstance()

        def result = new BlockingVariable(5)
        boolean valueSet = false
        execController.start {
            eanCodeEnhancer.enhance([(productKey): product], encoded).subscribe({
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
    void "enhance no eancode in feed"() {
        String feed = '<products></products>'
        mockHttpClient.demand.with {
            doGet(1) { String url ->
                assert url == "/eancode/A"
                rx.Observable.just(new Oct3HttpResponse(statusCode: 200, bodyAsBytes: feed.bytes))
            }
        }
        assert runEnhance(true, "a") == [sku: "a"]
    }


    @Test
    void "enhance not found from web service"() {
        mockHttpClient.demand.with {
            doGet(1) { String url ->
                assert url == "/eancode/A"
                rx.Observable.just(new Oct3HttpResponse(statusCode: 404))
            }
        }
        assert runEnhance(true, "a") == [sku: "a"]
    }

    def runEnhanceSuccess(boolean encoded, String product, String expectedUrl, String responseBody, String productKey = "sku") {
        mockHttpClient.demand.with {
            doGet(1) { String url ->
                assert url == expectedUrl
                rx.Observable.just(new Oct3HttpResponse(statusCode: 200, bodyAsBytes: responseBody.bytes))
            }
        }
        runEnhance(encoded, product, productKey)
    }

    @Test
    void "enhance with eancode"() {
        String feed = '''
            <products>
                <product>
                    <identifier type="display_name"><![CDATA[DSC-RX10]]></identifier>
                    <identifier type="catalogue_name"><![CDATA[DSC-RX10]]></identifier>
                    <identifier type="business_group"><![CDATA[DIM]]></identifier>
                    <identifier type="spider_business_group"><![CDATA[DIME]]></identifier>
                    <identifier type="sap_hierarchy"><![CDATA[SCPCDIMDSCCYBRCYBS]]></identifier>
                    <identifier type="eight_digit"><![CDATA[80814350]]></identifier>
                    <identifier type="material_name"><![CDATA[DSCRX10.CE3]]></identifier>
                    <identifier type="ean_code"><![CDATA[4905524328974]]></identifier>
                </product>
            </products>
            '''
        assert runEnhanceSuccess(false, "a/b+c", "/eancode/A_2FB_2BC", feed) == [sku: "a/b+c", eanCode: "4905524328974"]
    }

    @Test
    void "enhance with eancode for materialName param"() {
        String feed = '''
            <products>
                <product>
                    <identifier type="ean_code"><![CDATA[4905524328974]]></identifier>
                </product>
            </products>
            '''
        assert runEnhanceSuccess(false, "abc", "/eancode/ABC", feed, "materialName") == [materialName: "abc", eanCode: "4905524328974"]
    }

    @Test
    void "enhance with eancode encoded"() {
        String feed = '''
            <products>
                <product>
                    <identifier type="ean_code"><![CDATA[123]]></identifier>
                </product>
            </products>
            '''
        assert runEnhanceSuccess(true, "a_2fb_2bc", "/eancode/A_2FB_2BC", feed) == [sku: "a_2fb_2bc", eanCode: "123"]
    }

}
