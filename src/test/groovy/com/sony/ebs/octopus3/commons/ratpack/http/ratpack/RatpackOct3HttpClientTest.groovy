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
class RatpackOct3HttpClientTest {

    ExecController execController
    LaunchConfig launchConfig

    static Runner runner
    static String serviceUrl

    RatpackOct3HttpClient httpClient

    @BeforeClass
    static void initOnce() {
        def server = httpserver(8000 + RandomUtils.nextInt(999))
        server.get(by(uri("/test1"))).response("aaa")

        server.post(by(uri("/test2"))).response("bbb")

        server.delete(by(uri("/test3"))).response("ccc")

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
        httpClient = new RatpackOct3HttpClient(launchConfig)
    }

    @After
    void tearDown() {
        if (execController) execController.close()
    }

    def runHttpClient(httpExecution) {
        def result = new BlockingVariable(5)
        execController.start {
            httpExecution().subscribe({
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
        def url = serviceUrl + "/test1"
        def response = runHttpClient({
            httpClient.doGet(url)
        }) as Oct3HttpResponse

        assert response.statusCode == 200
        assert response.bodyAsBytes == "aaa".getBytes("UTF-8")
        assert response.uri.toString() == url
    }

    @Test
    void "test post"() {
        def url = serviceUrl + "/test2"
        def response = runHttpClient({
            httpClient.doPost(url, "xxx".getBytes("UTF-8"))
        }) as Oct3HttpResponse

        assert response.statusCode == 200
        assert response.bodyAsBytes == "bbb".getBytes("UTF-8")
        assert response.uri.toString() == url
    }

    @Test
    void "test delete"() {
        def url = serviceUrl + "/test3"
        def response = runHttpClient({
            httpClient.doDelete(url)
        }) as Oct3HttpResponse

        assert response.statusCode == 200
        assert response.bodyAsBytes == "ccc".getBytes("UTF-8")
        assert response.uri.toString() == url
    }

}
