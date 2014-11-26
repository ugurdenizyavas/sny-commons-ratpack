package com.sony.ebs.octopus3.commons.ratpack.product.cadc.delta.model

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import com.sony.ebs.octopus3.commons.flows.RepoValue
import com.sony.ebs.octopus3.commons.urn.URN
import com.sony.ebs.octopus3.commons.urn.URNImpl
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import groovy.util.logging.Slf4j

@ToString(includeNames = true, includePackage = false, ignoreNulls = true)
@EqualsAndHashCode(includes = ['type', 'publication', 'locale', 'materialName', 'processId'])
@JsonInclude(JsonInclude.Include.NON_NULL)
@Slf4j
class RepoProduct {

    RepoValue type
    String publication
    String locale
    String sku
    String processId
    String category
    String environment
    boolean upload

    @JsonIgnore
    URN getUrn() {
        if (type == RepoValue.category) {
            new URNImpl(type?.toString(), [environment, publication, locale])
        } else {
            new URNImpl(type?.toString(), [publication, locale, sku])
        }
    }

    URN getUrnForType(RepoValue prmType) {
        new URNImpl(prmType?.toString(), [publication, locale, sku])
    }

    URN getUrnForSubType(RepoValue prmSubType) {
        new URNImpl(type?.toString(), [prmSubType?.toString(), publication, locale, sku])
    }
}
