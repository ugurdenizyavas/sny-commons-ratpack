package com.sony.ebs.octopus3.commons.ratpack.encoding

import org.junit.Test

class ProductUtilTest {

    @Test
    void "test formatLocale lowercase"() {
        assert ProductUtil.formatLocale("fr_be") == "fr_BE"
    }

    @Test
    void "test formatLocale mixed"() {
        assert ProductUtil.formatLocale("fR_bE") == "fr_BE"
    }

    @Test(expected = IllegalArgumentException.class)
    void "test formatLocale invalid"() {
        ProductUtil.formatLocale("fr#be")
    }

    @Test
    void "test formatPublication"() {
        assert ProductUtil.formatPublication("scoRE-edi") == "SCORE-EDI"
    }

}
