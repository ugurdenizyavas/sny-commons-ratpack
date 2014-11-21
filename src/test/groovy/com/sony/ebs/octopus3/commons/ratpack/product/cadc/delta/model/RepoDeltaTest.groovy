package com.sony.ebs.octopus3.commons.ratpack.product.cadc.delta.model

import com.sony.ebs.octopus3.commons.flows.RepoValue
import org.junit.Before
import org.junit.Test

class RepoDeltaTest {

    RepoDelta delta

    @Before
    void before() {
        delta = new RepoDelta(type: RepoValue.flixMedia, publication: "SCORE", locale: "en_GB")
    }

    @Test
    void "lastModifiedUrn"() {
        assert delta.lastModifiedUrn.toString() == "urn:flixmedia:last_modified:score:en_gb"
    }

    @Test
    void "getUrnForType"() {
        assert delta.getUrnForType(RepoValue.flix_sku).toString() == "urn:flix_sku:score:en_gb"
    }

    @Test
    void "baseUrn"() {
        assert delta.baseUrn.toString() == "urn:flixmedia:score:en_gb"
    }

}
