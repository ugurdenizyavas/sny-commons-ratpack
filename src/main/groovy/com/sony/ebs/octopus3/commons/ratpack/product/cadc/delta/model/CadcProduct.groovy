package com.sony.ebs.octopus3.commons.ratpack.product.cadc.delta.model

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import com.sony.ebs.octopus3.commons.urn.URN
import com.sony.ebs.octopus3.commons.urn.URNImpl
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import groovy.util.logging.Slf4j

@ToString(includeNames = true, includePackage = false, ignoreNulls = true, includes = ['type', 'publication', 'locale', 'url', 'materialName', 'processId'])
@EqualsAndHashCode(includes = ['type', 'publication', 'locale', 'url', 'materialName', 'processId'])
@JsonInclude(JsonInclude.Include.NON_NULL)
@Slf4j
class CadcProduct {

    DeltaType type
    String publication
    String locale
    String url
    String materialName
    String processId

    @JsonIgnore
    List errors = []

    @JsonIgnore
    URN getUrn() {
        new URNImpl(type?.toString(), [publication, locale, materialName])
    }

    URN getUrnForType(DeltaType prmType) {
        new URNImpl(prmType?.toString(), [publication, locale, materialName])
    }

    URN getUrnForSubType(DeltaType prmSubType) {
        new URNImpl(type?.toString(), [prmSubType?.toString(), publication, locale, materialName])
    }

}
