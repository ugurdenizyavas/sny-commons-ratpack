package com.sony.ebs.octopus3.commons.ratpack.product.cadc.delta.model

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import com.sony.ebs.octopus3.commons.flows.RepoValue
import com.sony.ebs.octopus3.commons.process.ProcessId
import com.sony.ebs.octopus3.commons.urn.URN
import com.sony.ebs.octopus3.commons.urn.URNImpl
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import groovy.util.logging.Slf4j

@ToString(includeNames = true, includePackage = false, ignoreNulls = true,excludes = ['urlList'])
@EqualsAndHashCode(excludes = ['urlList'])
@JsonInclude(JsonInclude.Include.NON_NULL)
@Slf4j
class CadcDelta {

    RepoValue type
    String publication
    String locale
    String since
    String cadcUrl
    ProcessId processId

    @JsonIgnore
    URN getLastModifiedUrn() {
        new URNImpl(type?.toString(), [RepoValue.last_modified.toString(), publication, locale])
    }

}
