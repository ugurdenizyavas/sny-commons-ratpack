package com.sony.ebs.octopus3.commons.ratpack.http.ning

import com.github.dreamhead.moco.Runner
import com.ning.http.client.Response
import groovy.util.logging.Slf4j
import org.apache.commons.lang.math.RandomUtils
import org.junit.*
import ratpack.launch.LaunchConfig
import ratpack.launch.LaunchConfigBuilder
import spock.util.concurrent.BlockingVariable

import static com.github.dreamhead.moco.Moco.*

@Slf4j
class NingHttpClientTest {

    static LaunchConfig launchConfig
    NingHttpClient ningHttpClient

    static Runner runner
    static String serverUrl

    @BeforeClass
    static void initOnce() {
        def server = httpserver(8000 + RandomUtils.nextInt(999))
        server.get(by(uri("/test1"))).response("xxx")

        runner = Runner.runner(server)
        runner.start()
        serverUrl = "http://localhost:${server.port()}"

        launchConfig = LaunchConfigBuilder.noBaseDir().build()
    }

    @AfterClass
    static void tearDownOnce() {
        if (launchConfig) launchConfig.execController.close()
        runner.stop()
    }

    @Before
    void before() {
        ningHttpClient = new NingHttpClient(launchConfig)
    }

    Response runFlow(String url) {
        def result = new BlockingVariable<String>(5)
        launchConfig.execController.start {
            ningHttpClient.doGet(url).subscribe({
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
        def result = runFlow(serverUrl + "/test1")
        assert result.statusCode == 200
        assert result.responseBody == "xxx"
    }

    @Test
    void "test error"() {
        def result = runFlow(serverUrl + "/test2")
        assert result.statusCode == 400
    }

}
