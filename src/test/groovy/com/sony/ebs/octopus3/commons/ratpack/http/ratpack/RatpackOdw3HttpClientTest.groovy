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

        runner = Runner.runner(server)
        runner.start()
        serviceUrl = "http://localhost:${server.port()}/test1"
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

    def runGet(String url) {
        def result = new BlockingVariable(5)
        boolean valueSet = false
        execController.start {
            httpClient.doGet(url).subscribe({
                valueSet = true
                result.set(it)
            }, {
                log.error "error in flow", it
                result.set("error")
            }, {
                if (!valueSet) result.set("outOfFlow")
            })
        }
        result.get()
    }

    @Test
    void "test"() {
        def result = runGet(serviceUrl) as Oct3HttpResponse
        assert result.statusCode == 200
        assert result.bodyAsBytes == "aaa".getBytes("UTF-8")
    }

}
