package com.sony.ebs.octopus3.commons.ratpack.product.cadc.delta.model

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import com.sony.ebs.octopus3.commons.urn.URN
import com.sony.ebs.octopus3.commons.urn.URNImpl
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import groovy.util.logging.Slf4j

@ToString(includeNames = true, includePackage = false, ignoreNulls = true, includes = ['type', 'publication', 'locale', 'sku', 'eanCode','processId'])
@EqualsAndHashCode(includes = ['type', 'publication', 'locale', 'materialName', 'processId'])
@JsonInclude(JsonInclude.Include.NON_NULL)
@Slf4j
class RepoProduct {

    DeltaType type
    String publication
    String locale
    String sku
    String processId
    String eanCode

    @JsonIgnore
    List errors = []

    @JsonIgnore
    URN getUrn() {
        new URNImpl(type?.toString(), [publication, locale, sku])
    }

    URN getUrnForSubType(DeltaType prmSubType) {
        new URNImpl(type?.toString(), [prmSubType?.toString(), publication, locale, sku])
    }

}
