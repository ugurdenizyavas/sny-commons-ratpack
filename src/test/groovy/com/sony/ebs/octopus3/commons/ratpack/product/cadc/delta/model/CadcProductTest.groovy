package com.sony.ebs.octopus3.commons.ratpack.product.cadc.delta.model

import com.sony.ebs.octopus3.commons.flows.RepoValue
import org.junit.Before
import org.junit.Test

class CadcProductTest {

    CadcProduct product

    @Before
    void before() {
        product = new CadcProduct(type: RepoValue.global_sku, publication: "SCORE", locale: "en_GB")
    }

    @Test
    void "getOutUrn"() {
        assert product.getOutputUrn("x1").toString() == "urn:global_sku:score:en_gb:x1"
    }

}
