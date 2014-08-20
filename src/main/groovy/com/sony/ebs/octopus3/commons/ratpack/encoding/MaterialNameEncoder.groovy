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

    //private static final String charsToBeRemoved = "[\\/?#\\[\\]@!$&'()*+,;=]+";
    private static final String charsToBeRemoved = '[\\/?#\\[\\]@!$&\'()*+,;=]+'

    private static final String consequentHyphens = "([-])\\1+"

    /**
     * Encodes MaterialName.
     *
     * @param name of type String
     * @return String
     */
    public static String encode(String name) {
        if (name == null)
            return null

        // Replace the slashes
        name = name.replace("/", SLASH)
        name = name.replace("\\", BACKSLASH)
        name = name.replace("+", PLUS)

        try {
            String encodedName = URLEncoder.encode(name, "UTF-8")
            log.debug("Name {} is encoded to {}", name, encodedName)
            return encodedName
        }
        catch (Exception e) {
            log.error("Problem encoding url value for material name: " + name, e)
            return null
        }
    }

    /**
     * Decodes materialName
     *
     * @param name of type String
     * @return String
     */
    public static String decode(String name) {
        if (name == null)
            return null

        // Replace the slashes
        name = name.replace(SLASH, "/")
        name = name.replace(BACKSLASH, "\\")
        name = name.replace(PLUS, "%2B") // then %2B will be decoded to +

        try {
            String decodedName = URLDecoder.decode(name, "UTF-8")
            log.debug("Name {} is decoded to {}", name, decodedName)
            return decodedName
        }
        catch (Exception e) {
            log.error("Cannot decode materialName: " + name, e)
            return null
        }
    }

    /**
     * <ul>
     * 	<li>Replace the characters that are reserved as per <a href="http://tools.ietf.org/html/rfc3986#section-2.2">RFC 3986</a> with a hyphen. 
     * 		These are the characters that are reserved as per RFC 3986 with a hyphen: , ”/” , ”?” , ”#” , “[” , “]” , ”@” , ”!” , ”$” , “&” , ”’” , “(” , “)” , ”*” , ”+” , ”,” , ”;” , ”=”
     * 	<li>Replace whitespace with a hyphen.
     * 	<li>Replace multiple consequent hyphens with a single hyphen.
     * </ul>
     * @param input
     * @return sanitized string
     */
    public static String sanitizeString(String input) {
        if (StringUtils.isNotEmpty(input)) {
            return input.replaceAll(charsToBeRemoved, "-").replaceAll("\\s", "-").replaceAll(consequentHyphens, "-")
        }
        return null
    }

}

