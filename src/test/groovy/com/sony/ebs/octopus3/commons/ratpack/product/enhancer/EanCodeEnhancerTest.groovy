package com.sony.ebs.octopus3.commons.ratpack.product.enhancer

import org.junit.Before
import org.junit.Test

class EanCodeEnhancerTest {

    EanCodeEnhancer eanCodeEnhancer

    @Before
    void before() {
        eanCodeEnhancer = new EanCodeEnhancer()
    }

    @Test
    void "test DSC-500"() {
        assert "4905524328974" == eanCodeEnhancer.parseFeed("DSC-500", '<eancodes><eancode material="DSC-500" code="4905524328974"/></eancodes>')
    }

    @Test
    void "test for null"() {
        assert null == eanCodeEnhancer.parseFeed("DSC-500", '<eancodes></eancodes>')
    }
}
