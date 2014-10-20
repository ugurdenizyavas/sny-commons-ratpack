package com.sony.ebs.octopus3.commons.ratpack.http.ratpack

import com.github.dreamhead.moco.Runner
import com.sony.ebs.octopus3.commons.ratpack.http.Oct3HttpResponse
import groovy.util.logging.Slf4j
import org.apache.commons.lang.math.RandomUtils
import org.junit.After
import org.junit.AfterClass
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import ratpack.exec.ExecController
import ratpack.launch.LaunchConfig
import ratpack.launch.LaunchConfigBuilder
import spock.util.concurrent.BlockingVariable

import static com.github.dreamhead.moco.Moco.by
import static com.github.dreamhead.moco.Moco.httpserver
import static com.github.dreamhead.moco.Moco.uri

@Slf4j
class RatpackOdw3HttpClientTest {

    ExecController execController
    LaunchConfig launchConfig

    static Runner runner
    static String serviceUrl

    RatpackOdw3HttpClient httpClient

    @BeforeClass
    static void initOnce() {
        def server = httpserver(8000 + RandomUtils.nextInt(999))
        server.get(by(uri("/test1"))).response("aaa")

        server.post(by(uri("/test2"))).response("bbb")

        runner = Runner.runner(server)
        runner.start()
        serviceUrl = "http://localhost:${server.port()}"
    }

    @AfterClass
    static void tearDownOnce() {
        runner.stop()
    }

    @Before
    void init() {
        launchConfig = LaunchConfigBuilder.noBaseDir().build()
        execController = launchConfig.execController
        httpClient = new RatpackOdw3HttpClient(launchConfig)
    }

    @After
    void tearDown() {
        if (execController) execController.close()
    }

    def runHttpClient(boolean isGetRequest, String url, String data = null) {
        def result = new BlockingVariable(5)
        String finalUrl = serviceUrl + url
        execController.start {
            (isGetRequest ? httpClient.doGet(finalUrl) : httpClient.doPost(finalUrl, data.getBytes("UTF-8"))).subscribe({
                result.set(it)
            }, {
                log.error "error in flow", it
                result.set("error")
            })
        }
        result.get()
    }

    @Test
    void "test get"() {
        def response = runHttpClient(true, "/test1") as Oct3HttpResponse
        assert response.statusCode == 200
        assert response.bodyAsBytes == "aaa".getBytes("UTF-8")
        assert response.uri.toString() == serviceUrl + "/test1"
    }

    @Test
    void "test post"() {
        def response = runHttpClient(false, "/test2", "xxx") as Oct3HttpResponse
        assert response.statusCode == 200
        assert response.bodyAsBytes == "bbb".getBytes("UTF-8")
        assert response.uri.toString() == serviceUrl + "/test2"
    }

}
