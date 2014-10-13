package com.sony.ebs.octopus3.commons.ratpack.product.cadc.delta.model

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import com.sony.ebs.octopus3.commons.process.ProcessId
import com.sony.ebs.octopus3.commons.urn.URN
import com.sony.ebs.octopus3.commons.urn.URNImpl
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import groovy.util.logging.Slf4j

@ToString(includeNames = true, includePackage = false, ignoreNulls = true, includes = ['type', 'publication', 'locale', 'sdate', 'edate', 'processId'])
@EqualsAndHashCode(includes = ['type', 'publication', 'locale', 'sdate', 'edate', 'processId'])
@JsonInclude(JsonInclude.Include.NON_NULL)
@Slf4j
class RepoDelta {

    DeltaType type
    String publication
    String locale
    String sdate
    String edate
    ProcessId processId

    String finalStartDate
    String finalDeltaUrl
    List deltaUrns

    @JsonIgnore
    List errors = []

    @JsonIgnore
    URN getLastModifiedUrn() {
        new URNImpl(type.toString(), [DeltaType.last_modified.toString(), publication, locale])
    }

    @JsonIgnore
    URN getBaseUrn() {
        new URNImpl(type.toString(), [publication, locale])
    }

    URN getUrnForType(DeltaType prmType) {
        new URNImpl(prmType?.toString(), [publication, locale])
    }

}
