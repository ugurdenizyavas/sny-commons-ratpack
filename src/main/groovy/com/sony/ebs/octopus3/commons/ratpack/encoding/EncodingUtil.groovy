package com.sony.ebs.octopus3.commons.ratpack.encoding

import java.nio.charset.Charset

class EncodingUtil {

    public static final String CHARSET_STR = 'UTF-8'

    static final Locale LOCALE = Locale.US

    public static final Charset CHARSET = Charset.forName(CHARSET_STR)
}
