package com.sony.ebs.octopus3.commons.ratpack.product.cadc.delta.model

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import com.sony.ebs.octopus3.commons.flows.Delta
import com.sony.ebs.octopus3.commons.flows.RepoValue
import com.sony.ebs.octopus3.commons.process.ProcessId
import com.sony.ebs.octopus3.commons.urn.URN
import com.sony.ebs.octopus3.commons.urn.URNImpl
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import groovy.util.logging.Slf4j

@ToString(includeNames = true, includePackage = false, ignoreNulls = true)
@EqualsAndHashCode
@JsonInclude(JsonInclude.Include.NON_NULL)
@Slf4j
class CadcDelta extends Delta implements Serializable {

    String cadcUrl

    @JsonIgnore
    URN getLastModifiedUrn() {
        new URNImpl(type?.toString(), [RepoValue.last_modified.toString(), publication, locale])
    }

}
