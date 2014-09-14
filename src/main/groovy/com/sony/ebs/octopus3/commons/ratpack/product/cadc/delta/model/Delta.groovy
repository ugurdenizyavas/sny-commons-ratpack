package com.sony.ebs.octopus3.commons.ratpack.product.cadc.delta.model

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import com.sony.ebs.octopus3.commons.process.ProcessId
import com.sony.ebs.octopus3.commons.urn.URN
import com.sony.ebs.octopus3.commons.urn.URNImpl
import groovy.transform.ToString

@ToString(includeNames = true, includePackage = false, ignoreNulls = true, includes = ['processId', 'publication', 'locale', 'since', 'cadcUrl'])
@JsonInclude(JsonInclude.Include.NON_NULL)
class Delta {

    ProcessId processId
    String publication
    String locale
    String since
    String cadcUrl
    DeltaUrnValue type

    String finalSince
    String finalCadcUrl

    @JsonIgnore
    List urlList

    @JsonIgnore
    List errors = []

    @JsonIgnore
    URN getLastModifiedUrn() {
        new URNImpl(type?.toString(), [DeltaUrnValue.last_modified.toString(), publication, locale])
    }

}
