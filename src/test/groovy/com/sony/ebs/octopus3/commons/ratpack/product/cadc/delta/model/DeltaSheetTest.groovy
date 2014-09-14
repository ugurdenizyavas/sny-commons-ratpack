package com.sony.ebs.octopus3.commons.ratpack.product.cadc.delta.model

import org.junit.Before
import org.junit.Test

class DeltaSheetTest {

    DeltaItem deltaItem

    @Before
    void before() {
        deltaItem = new DeltaItem(type: DeltaUrnValue.global_sku, publication: "SCORE", locale: "en_GB", url: "//")
    }

    @Test
    void "setUrnStr"() {
        deltaItem.assignUrnStr("aaa")
        assert deltaItem.urnStr == "urn:global_sku:score:en_gb:aaa"
    }

}
