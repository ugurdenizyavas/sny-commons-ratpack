package com.sony.ebs.octopus3.commons.ratpack.handlers

import groovy.util.logging.Slf4j
import org.joda.time.DateTime
import org.joda.time.Period
import org.junit.Test

@Slf4j
class HandlerUtilTest {

    @Test
    void "test period formatter"() {
        assert "02:35:123" == HandlerUtil.PERIOD_FORMATTER.print(new Period(155123))
    }

    @Test
    void "test getErrorMessage"() {
        assert "aaa" == HandlerUtil.getErrorMessage(new Exception("aaa"))
    }

    @Test
    void "test getErrorMessage for null "() {
        assert null == HandlerUtil.getErrorMessage(null)
    }

    @Test
    void "test getErrorMessage for no message "() {
        assert null == HandlerUtil.getErrorMessage(new Exception())
    }

    @Test
    void "test getErrorMessage with cause"() {
        assert "bbb" == HandlerUtil.getErrorMessage(new Exception("", new Exception("bbb")))
    }

    @Test
    void "test getErrorMessage for no message  with cause"() {
        assert null == HandlerUtil.getErrorMessage(new Exception("", new Exception()))
    }

}
