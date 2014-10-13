package com.sony.ebs.octopus3.commons.ratpack.encoding

import groovy.util.logging.Slf4j
import org.junit.Test

@Slf4j
public class MaterialNameEncoderTest {

    @Test
    public void decode() {
        assert "DSC-F828/CEE" == MaterialNameEncoder.decode("DSC-F828_2FCEE")
        assert "DSC-F828\\CEE" == MaterialNameEncoder.decode("dsc-f828_5ccee")
        assert "DSC-F828" == MaterialNameEncoder.decode("DSC-F828")
        assert "DSC-F828.CEE" == MaterialNameEncoder.decode("dsc-f828.cee")
        assert "DSC-F828+A.CEE" == MaterialNameEncoder.decode("DSC-F828_2ba.CEE")
    }

    @Test
    public void encode() {
        assert "DSC-F828_2FCEE" == MaterialNameEncoder.encode("dsc-f828/cee")
        assert "DSC-F828_5CCEE" == MaterialNameEncoder.encode("DSC-F828\\CEE")
        assert "DSC-F828" == MaterialNameEncoder.encode("DSC-F828")
        assert "DSC-F828.CEE" == MaterialNameEncoder.encode("dsc-F828.CEE")
        assert "DSC-F828_2BA.CEE" == MaterialNameEncoder.encode("DSC-F828+a.cee")
    }

    @Test
    public void encodeWithSpaces() {
        assert "DSC-F828++CEE" == MaterialNameEncoder.encode("DSC-F828  CEE")
        assert "DSC-F828+_2B+CEE" == MaterialNameEncoder.encode("DSC-F828 + CEE")
    }

}
