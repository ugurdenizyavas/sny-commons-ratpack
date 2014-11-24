package com.sony.ebs.octopus3.commons.ratpack.product.cadc.delta.model

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import groovy.transform.EqualsAndHashCode
import groovy.transform.Sortable
import groovy.transform.ToString
import groovy.util.logging.Slf4j

@ToString(includeNames = true, includePackage = false, ignoreNulls = true)
@EqualsAndHashCode
@JsonInclude(JsonInclude.Include.NON_NULL)
@Sortable(includes = ['inputUrn'])
@Slf4j
class ProductResult {

    String sku

    String inputUrn
    String inputUrl
    String outputUrn
    String outputUrl

    int statusCode
    boolean success
    String eanCode
    String category

    @JsonIgnore
    List<String> errors = []

    Map other

}
