package com.sony.ebs.octopus3.commons.ratpack.product.cadc.delta.model

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import com.sony.ebs.octopus3.commons.urn.URNImpl
import groovy.transform.ToString
import groovy.util.logging.Slf4j

@ToString(includeNames = true, includePackage = false, ignoreNulls = true, includes = ['publication', 'locale', 'processId', 'url'])
@JsonInclude(JsonInclude.Include.NON_NULL)
@Slf4j
class DeltaItem {

    String publication
    String locale
    String url
    String processId

    DeltaUrnValue type

    String urnStr

    @JsonIgnore
    List errors = []

    void assignUrnStr(String materialName) {
        urnStr = new URNImpl(type?.toString(), [publication, locale, materialName]).toString()
    }

}
