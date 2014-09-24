package com.sony.ebs.octopus3.commons.ratpack.product.cadc.delta.model

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import com.sony.ebs.octopus3.commons.process.ProcessId
import com.sony.ebs.octopus3.commons.urn.URN
import com.sony.ebs.octopus3.commons.urn.URNImpl
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import groovy.util.logging.Slf4j

@ToString(includeNames = true, includePackage = false, ignoreNulls = true, includes = ['type', 'publication', 'locale', 'sdate', 'edate','processId'])
@EqualsAndHashCode(includes = ['type', 'publication', 'locale', 'sdate', 'edate','processId'])
@JsonInclude(JsonInclude.Include.NON_NULL)
@Slf4j
class DeltaRepo {

    DeltaType type
    String publication
    String locale
    String sdate
    String edate

    ProcessId processId

    List deltaUrns

    @JsonIgnore
    List errors = []

    @JsonIgnore
    URN getDeltaUrn() {
        new URNImpl(DeltaType.global_sku.toString(), [publication, locale])
    }

    @JsonIgnore
    URN getLastModifiedUrn() {
        new URNImpl(DeltaType.global_sku.toString(), [DeltaType.last_modified.toString(), publication, locale])
    }

    @JsonIgnore
    URN getBaseUrn() {
        new URNImpl(type.toString(), [publication, locale])
    }

}
