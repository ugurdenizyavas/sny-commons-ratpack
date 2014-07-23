package com.sony.ebs.octopus3.commons.ratpack.http.ning

import com.github.dreamhead.moco.Runner
import groovy.util.logging.Slf4j
import org.apache.commons.lang.math.RandomUtils
import org.junit.*
import ratpack.exec.ExecController
import ratpack.launch.LaunchConfigBuilder
import spock.util.concurrent.BlockingVariable

import static com.github.dreamhead.moco.Moco.*

@Slf4j
class NingHttpClientTest {

    static ExecController execController
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

        execController = LaunchConfigBuilder.noBaseDir().build().execController
    }

    @AfterClass
    static void tearDownOnce() {
        if (execController) execController.close()
        runner.stop()
    }

    @Before
    void before() {
        ningHttpClient = new NingHttpClient(execController.control)
    }

    void runFlow(String url, String expected) {
        def result = new BlockingVariable<String>(5)
        execController.start {
            ningHttpClient.doGet(url).subscribe({
                result.set(it)
            }, {
                log.error "error in flow", it
                result.set("error")
            })
        }
        assert result.get() == expected
    }

    @Test
    void "test get"() {
        runFlow(serverUrl + "/test1", "xxx")
    }

    @Test
    void "test error"() {
        runFlow(serverUrl + "/test2", "error")
    }

}
