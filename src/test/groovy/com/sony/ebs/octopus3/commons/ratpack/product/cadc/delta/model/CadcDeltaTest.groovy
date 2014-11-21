package com.sony.ebs.octopus3.commons.ratpack.product.cadc.delta.model

import com.sony.ebs.octopus3.commons.flows.RepoValue
import org.junit.Before
import org.junit.Test

class CadcDeltaTest {

    CadcDelta delta

    @Before
    void before() {
        delta = new CadcDelta(type: RepoValue.flix_sku, publication: "SCORE", locale: "en_GB")
    }

    @Test
    void "lastModifiedUrn"() {
        assert delta.lastModifiedUrn.toString() == "urn:flix_sku:last_modified:score:en_gb"
    }

}
