package com.sony.ebs.octopus3.commons.ratpack.product.cadc.delta.model

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import groovy.util.logging.Slf4j

@ToString(includeNames = true, includePackage = false, ignoreNulls = true)
@EqualsAndHashCode
@JsonInclude(JsonInclude.Include.NON_NULL)
@Slf4j
class DeltaResult {

    List<String> deltaUrns
    List<String> categoryFilteredOutUrns
    List<String> eanCodeFilteredOutUrns
    List<String> successfulUrns
    List<String> unsuccessfulUrns
    Map productErrors = [:]
    Map other = [:]

    String finalStartDate
    String finalDeltaUrl

    @JsonIgnore
    List<String> deltaUrls

    @JsonIgnore
    List<String> errors = []

}
