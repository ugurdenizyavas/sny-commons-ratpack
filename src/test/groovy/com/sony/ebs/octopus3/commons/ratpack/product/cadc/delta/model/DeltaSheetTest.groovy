package com.sony.ebs.octopus3.commons.ratpack.product.cadc.delta.model

import org.junit.Before
import org.junit.Test

class DeltaSheetTest {

    DeltaItem deltaItem

    @Before
    void before() {
        deltaItem = new DeltaItem(type: DeltaType.global_sku, publication: "SCORE", locale: "en_GB", materialName: "aaa")
    }

    @Test
    void "getUrnForSubType"() {
        assert deltaItem.getUrnForSubType(DeltaType.xml).toString() == "urn:global_sku:xml:score:en_gb:aaa"
    }

}
