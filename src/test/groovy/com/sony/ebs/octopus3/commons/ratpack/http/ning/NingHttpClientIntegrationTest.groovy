package com.sony.ebs.octopus3.commons.ratpack.http.ning

import com.github.dreamhead.moco.Runner
import groovy.util.logging.Slf4j
import org.apache.commons.lang.math.RandomUtils
import org.junit.*
import ratpack.exec.ExecController
import ratpack.launch.LaunchConfigBuilder

import static com.github.dreamhead.moco.Moco.*

@Slf4j
class NingHttpClientIntegrationTest {

    ExecController execController
    NingHttpClient ningHttpClient

    static Runner runner
    static String serverUrl

    @BeforeClass
    static void initOnce() {
        def server = httpserver(8000 + RandomUtils.nextInt(999))
        server.get(by(uri("/test1"))).response("xxx")

        runner = Runner.runner(server)
        runner.start()
        serverUrl = "http://localhost:${server.port()}/test1"
    }

    @AfterClass
    static void tearDownOnce() {
        runner.stop()
    }

    @Before
    void before() {
        execController = LaunchConfigBuilder.noBaseDir().build().execController
        ningHttpClient = new NingHttpClient(execController.control)
    }

    @After
    void after() {
        if (execController) execController.close()
    }

    @Test
    void "test get"() {
        def finished = new Object()
        def result
        execController.start {
            ningHttpClient.doGet(serverUrl).subscribe { String res ->
                synchronized (finished) {
                    result = res
                    finished.notifyAll()
                }
            }
        }
        synchronized (finished) {
            finished.wait 5000
        }
        assert result == "xxx"
    }

}
