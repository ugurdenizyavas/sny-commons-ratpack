package com.sony.ebs.octopus3.commons.ratpack.product.cadc.delta.model

import com.sony.ebs.octopus3.commons.flows.RepoValue
import org.junit.Before
import org.junit.Test

class CadcProductTest {

    CadcProduct product

    @Before
    void before() {
        product = new CadcProduct(type: RepoValue.global_sku, publication: "SCORE", locale: "en_GB", materialName: "aaa")
    }

    @Test
    void "getUrnForSubType"() {
        assert product.getUrnForSubType(RepoValue.previous).toString() == "urn:global_sku:previous:score:en_gb:aaa"
    }

}
