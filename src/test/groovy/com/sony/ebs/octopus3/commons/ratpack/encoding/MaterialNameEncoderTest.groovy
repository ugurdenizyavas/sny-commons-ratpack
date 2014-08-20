package com.sony.ebs.octopus3.commons.ratpack.encoding

import groovy.util.logging.Slf4j
import org.junit.Test

@Slf4j
public class MaterialNameEncoderTest {

    @Test
    public void decodingMaterialName() {
        assert "Decoded material name is wrong.", "DSC-F828/CEE" == MaterialNameEncoder.decode("DSC-F828_2FCEE")
        assert "Decoded material name is wrong.", "DSC-F828\\CEE" == MaterialNameEncoder.decode("DSC-F828_5CCEE")
        assert "Decoded material name is wrong.", "DSC-F828" == MaterialNameEncoder.decode("DSC-F828")
        assert "Decoded material name is wrong.", "DSC-F828.CEE" == MaterialNameEncoder.decode("DSC-F828.CEE")
        assert "Decoded material name is wrong.", "DSC-F828+A.CEE" == MaterialNameEncoder.decode("DSC-F828_2BA.CEE")
    }

    @Test
    public void encodingMaterialName() {
        assert "Encoded material name is wrong.", "DSC-F828_2FCEE" == MaterialNameEncoder.encode("DSC-F828/CEE")
        assert "Encoded material name is wrong.", "DSC-F828_5CCEE" == MaterialNameEncoder.encode("DSC-F828\\CEE")
        assert "Encoded material name is wrong.", "DSC-F828" == MaterialNameEncoder.encode("DSC-F828")
        assert "Encoded material name is wrong.", "DSC-F828.CEE" == MaterialNameEncoder.encode("DSC-F828.CEE")
        assert "Encoded material name is wrong.", "DSC-F828_2BA.CEE" == MaterialNameEncoder.encode("DSC-F828+A.CEE")
    }

    @Test
    public void encodingMaterialNameHavingSpace() {
        assert "Encoded material name is wrong.", "DSC-F828++CEE" == MaterialNameEncoder.encode("DSC-F828  CEE")
        assert "Encoded material name is wrong.", "DSC-F828+_2B+CEE" == MaterialNameEncoder.encode("DSC-F828 + CEE")
    }

    public void sanitizeString() {
        assert "DSC-F828-CEE" == MaterialNameEncoder.sanitizeString('DSC-[-]-$-F828++CEE')
    }

}
