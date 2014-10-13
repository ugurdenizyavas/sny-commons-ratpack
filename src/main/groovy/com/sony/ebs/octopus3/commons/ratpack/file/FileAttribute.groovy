package com.sony.ebs.octopus3.commons.ratpack.file

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@EqualsAndHashCode
@ToString(includePackage = false, includeNames = true)
class FileAttribute {
    boolean found
    String value
}
