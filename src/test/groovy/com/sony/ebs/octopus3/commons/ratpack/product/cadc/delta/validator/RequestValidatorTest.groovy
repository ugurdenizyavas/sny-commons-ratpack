package com.sony.ebs.octopus3.commons.ratpack.product.cadc.delta.validator

import com.sony.ebs.octopus3.commons.ratpack.product.cadc.delta.model.CadcDelta
import com.sony.ebs.octopus3.commons.ratpack.product.cadc.delta.model.CadcProduct
import com.sony.ebs.octopus3.commons.ratpack.product.cadc.delta.model.DeltaType
import com.sony.ebs.octopus3.commons.ratpack.product.cadc.delta.model.RepoDelta
import com.sony.ebs.octopus3.commons.ratpack.product.cadc.delta.model.RepoProduct
import org.junit.Before
import org.junit.Test

class RequestValidatorTest {

    RequestValidator validator
    CadcDelta cadcDelta
    CadcProduct cadcProduct
    RepoDelta repoDelta
    RepoProduct repoProduct

    @Before
    void before() {
        validator = new RequestValidator()
        cadcDelta = new CadcDelta(type: DeltaType.global_sku, publication: "SCORE", locale: "en_GB", cadcUrl: "http://aaa/bbb", since: "2014-07-05T00:00:00.000Z")
        cadcProduct = new CadcProduct(type: DeltaType.global_sku, publication: "SCORE", locale: "en_GB", url: "//a")
        repoDelta = new RepoDelta(type: DeltaType.global_sheet, publication: "GLOBAL", locale: "en_GB")
        repoProduct = new RepoProduct(type: DeltaType.global_sheet, publication: "SCORE", locale: "en_GB", sku: "a")
    }

    @Test
    void "validate all"() {
        assert !validator.validateCadcDelta(cadcDelta)
    }

    @Test
    void "validate url no protocol"() {
        cadcDelta.cadcUrl = "//bbb"
        assert !validator.validateCadcDelta(cadcDelta)
    }

    @Test
    void "validate url no host"() {
        cadcDelta.cadcUrl = "/bbb"
        assert validator.validateCadcDelta(cadcDelta) == ["cadcUrl parameter is invalid"]
    }

    @Test
    void "validate url null"() {
        cadcDelta.cadcUrl = null
        assert validator.validateCadcDelta(cadcDelta) == ["cadcUrl parameter is invalid"]
    }

    @Test
    void "validate url empty"() {
        cadcDelta.cadcUrl = ""
        assert validator.validateCadcDelta(cadcDelta) == ["cadcUrl parameter is invalid"]
    }

    @Test
    void "validate since value null"() {
        cadcDelta.since = null
        assert !validator.validateCadcDelta(cadcDelta)
    }

    @Test
    void "validate since value empty"() {
        cadcDelta.since = ""
        assert !validator.validateCadcDelta(cadcDelta)
    }

    @Test
    void "validate since value all"() {
        cadcDelta.since = "All"
        assert !validator.validateCadcDelta(cadcDelta)
    }

    @Test
    void "validate since value invalid"() {
        cadcDelta.since = "2014-07-05T00-00:00.000Z"
        assert validator.validateCadcDelta(cadcDelta) == ["since parameter is invalid"]
    }

    @Test
    void "validate since value short and invalid"() {
        cadcDelta.since = "2014-07-05T00:00:00"
        assert validator.validateCadcDelta(cadcDelta) == ["since parameter is invalid"]
    }

    @Test
    void "validate locale null"() {
        cadcDelta.locale = null
        assert validator.validateCadcDelta(cadcDelta) == ["locale parameter is invalid"]
    }

    @Test
    void "validate locale empty"() {
        cadcDelta.locale = ""
        assert validator.validateCadcDelta(cadcDelta) == ["locale parameter is invalid"]
    }

    @Test
    void "validate locale invalid"() {
        cadcDelta.locale = "tr_T"
        assert validator.validateCadcDelta(cadcDelta) == ["locale parameter is invalid"]
    }

    @Test
    void "validate delta type null"() {
        cadcDelta.type = null
        assert validator.validateCadcDelta(cadcDelta) == ["type parameter is invalid"]
    }

    @Test
    void "validate publication null"() {
        cadcDelta.publication = null
        assert validator.validateCadcDelta(cadcDelta) == ["publication parameter is invalid"]
    }

    @Test
    void "validate publication empty"() {
        cadcDelta.publication = ""
        assert validator.validateCadcDelta(cadcDelta) == ["publication parameter is invalid"]
    }

    @Test
    void "validate publication invalid"() {
        cadcDelta.publication = "?aa"
        assert validator.validateCadcDelta(cadcDelta) == ["publication parameter is invalid"]
    }

    @Test
    void "validate publication with dash"() {
        cadcDelta.publication = "SCORE-EDITORIAL"
        assert !validator.validateCadcDelta(cadcDelta)
    }

    @Test
    void "validate publication with underscore"() {
        cadcDelta.publication = "SCORE_EDITORIAL"
        assert !validator.validateCadcDelta(cadcDelta)
    }

    @Test
    void "validate publication alphanumeric"() {
        cadcDelta.publication = "SONY1"
        assert !validator.validateCadcDelta(cadcDelta)
    }

    @Test
    void "validate delta item type null"() {
        cadcProduct.type = null
        assert validator.validateCadcProduct(cadcProduct) == ["type parameter is invalid"]
    }

    @Test
    void "validate delta item"() {
        assert !validator.validateCadcProduct(cadcProduct)
    }

    @Test
    void "validate delta item invalid url"() {
        cadcProduct.url = "/a"
        assert validator.validateCadcProduct(cadcProduct) == ["url parameter is invalid"]
    }

    @Test
    void "validate delta item invalid publication"() {
        cadcProduct.publication = null
        assert validator.validateCadcProduct(cadcProduct) == ["publication parameter is invalid"]
    }

    @Test
    void "validate delta item invalid locale"() {
        cadcProduct.locale = null
        assert validator.validateCadcProduct(cadcProduct) == ["locale parameter is invalid"]
    }

    @Test
    void "validate deltaRepo"() {
        assert !validator.validateRepoDelta(repoDelta)
    }

    @Test
    void "validate deltaRepo type null"() {
        repoDelta.with {
            type = null
        }
        assert validator.validateRepoDelta(repoDelta) == ["type parameter is invalid"]
    }

    @Test
    void "validate deltaRepo valid sdate "() {
        repoDelta.with {
            sdate = "2014-07-09T00:00:00.000Z"
        }
        assert !validator.validateRepoDelta(repoDelta)
    }

    @Test
    void "validate deltaRepo invalid sdate "() {
        repoDelta.with {
            sdate = "s1"
        }
        assert validator.validateRepoDelta(repoDelta) == ["sdate parameter is invalid"]
    }

    @Test
    void "validate deltaRepo valid edate "() {
        repoDelta.with {
            edate = "2014-07-09T00:00:00.000Z"
        }
        assert !validator.validateRepoDelta(repoDelta)
    }

    @Test
    void "validate deltaRepo invalid edate "() {
        repoDelta.with {
            edate = "s2"
        }
        assert validator.validateRepoDelta(repoDelta) == ["edate parameter is invalid"]
    }

    @Test
    void "validate repo delta item"() {
        assert !validator.validateRepoProduct(repoProduct)
    }

    @Test
    void "validate repo delta item type null"() {
        repoProduct.type = null
        assert validator.validateRepoProduct(repoProduct) == ["type parameter is invalid"]
    }

    @Test
    void "validate repo delta item invalid publication"() {
        repoProduct.publication = null
        assert validator.validateRepoProduct(repoProduct) == ["publication parameter is invalid"]
    }

    @Test
    void "validate repo delta item invalid locale"() {
        repoProduct.locale = null
        assert validator.validateRepoProduct(repoProduct) == ["locale parameter is invalid"]
    }

    @Test
    void "validate repo delta item invalid materialName"() {
        repoProduct.sku = null
        assert validator.validateRepoProduct(repoProduct) == ["sku parameter is invalid"]
    }

}