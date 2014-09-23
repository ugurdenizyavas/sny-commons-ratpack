package com.sony.ebs.octopus3.commons.ratpack.product.cadc.delta.model

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import com.sony.ebs.octopus3.commons.process.ProcessId
import com.sony.ebs.octopus3.commons.urn.URN
import com.sony.ebs.octopus3.commons.urn.URNImpl
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@ToString(includeNames = true, includePackage = false, ignoreNulls = true, includes = ['processId', 'publication', 'locale', 'sdate', 'edate'])
@EqualsAndHashCode
@JsonInclude(JsonInclude.Include.NON_NULL)
class DeltaRepo {

    DeltaType type
    ProcessId processId
    String publication
    String locale
    String sdate
    String edate
    List deltaUrns

    @JsonIgnore
    List errors = []

    @JsonIgnore
    URN getDeltaUrn() {
        new URNImpl(DeltaType.global_sku.toString(), [publication, locale])
    }

    @JsonIgnore
    URN getLastModifiedUrn() {
        new URNImpl(type.toString(), [DeltaType.last_modified.toString(), publication, locale])
    }

    @JsonIgnore
    URN getBaseUrn() {
        new URNImpl(type.toString(), [publication, locale])
    }

}
