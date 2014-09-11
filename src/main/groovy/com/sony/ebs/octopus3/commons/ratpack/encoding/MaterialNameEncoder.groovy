package com.sony.ebs.octopus3.commons.ratpack.encoding

import groovy.util.logging.Slf4j
import org.apache.commons.lang.StringUtils

/**
 * Provides static methods for encoding and decoding materialNames.
 */
@Slf4j
public class MaterialNameEncoder {

    private static final String BACKSLASH = "_5C"

    private static final String SLASH = "_2F"

    private static final String PLUS = "_2B"

    /**
     * Encodes MaterialName.
     *
     * @param name of type String
     * @return String
     */
    public static String encode(String name) {
        String newName = name
        if (name) {
            newName = newName.replace("/", SLASH)
            newName = newName.replace("\\", BACKSLASH)
            newName = newName.replace("+", PLUS)
            try {
                newName = URLEncoder.encode(newName, "UTF-8")
                log.trace("Name {} is encoded to {}", name, newName)
            } catch (e) {
                log.error("Problem encoding url value for material name: " + name, e)
                return null
            }
        }
        newName
    }

    /**
     * Decodes materialName
     *
     * @param name of type String
     * @return String
     */
    public static String decode(String name) {
        String newName = name
        if (name) {
            newName = newName.replace(SLASH, "/")
            newName = newName.replace(BACKSLASH, "\\")
            newName = newName.replace(PLUS, "%2B") // then %2B will be decoded to +
            try {
                newName = URLDecoder.decode(newName, "UTF-8")
                log.debug("Name {} is decoded to {}", name, newName)
            } catch (e) {
                log.error("Cannot decode materialName: " + name, e)
                return null
            }
        }
        newName
    }

}

