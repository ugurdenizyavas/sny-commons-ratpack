package com.sony.ebs.octopus3.commons.ratpack.product.cadc.delta.validator

import com.sony.ebs.octopus3.commons.ratpack.product.cadc.delta.model.CadcDelta
import com.sony.ebs.octopus3.commons.ratpack.product.cadc.delta.model.CadcProduct
import com.sony.ebs.octopus3.commons.ratpack.product.cadc.delta.model.DeltaRepo
import com.sony.ebs.octopus3.commons.ratpack.product.cadc.delta.model.DeltaRepoItem
import com.sony.ebs.octopus3.commons.ratpack.product.cadc.delta.model.DeltaType
import org.junit.Before
import org.junit.Test

class RequestValidatorTest {

    RequestValidator validator
    CadcDelta delta
    CadcProduct deltaItem
    DeltaRepo deltaRepo
    DeltaRepoItem deltaRepoItem

    @Before
    void before() {
        validator = new RequestValidator()
        delta = new CadcDelta(type: DeltaType.global_sku, publication: "SCORE", locale: "en_GB", cadcUrl: "http://aaa/bbb", since: "2014-07-05T00:00:00.000Z")
        deltaItem = new CadcProduct(type: DeltaType.global_sku, publication: "SCORE", locale: "en_GB", url: "//a")
        deltaRepo = new DeltaRepo(type: DeltaType.global_sheet, publication: "GLOBAL", locale: "en_GB")
        deltaRepoItem = new DeltaRepoItem(type: DeltaType.global_sheet, publication: "SCORE", locale: "en_GB", materialName: "a")
    }

    @Test
    void "validate all"() {
        assert !validator.validateCadcDelta(delta)
    }

    @Test
    void "validate url no protocol"() {
        delta.cadcUrl = "//bbb"
        assert !validator.validateCadcDelta(delta)
    }

    @Test
    void "validate url no host"() {
        delta.cadcUrl = "/bbb"
        assert validator.validateCadcDelta(delta) == ["cadcUrl parameter is invalid"]
    }

    @Test
    void "validate url null"() {
        delta.cadcUrl = null
        assert validator.validateCadcDelta(delta) == ["cadcUrl parameter is invalid"]
    }

    @Test
    void "validate url empty"() {
        delta.cadcUrl = ""
        assert validator.validateCadcDelta(delta) == ["cadcUrl parameter is invalid"]
    }

    @Test
    void "validate since value null"() {
        delta.since = null
        assert !validator.validateCadcDelta(delta)
    }

    @Test
    void "validate since value empty"() {
        delta.since = ""
        assert !validator.validateCadcDelta(delta)
    }

    @Test
    void "validate since value all"() {
        delta.since = "All"
        assert !validator.validateCadcDelta(delta)
    }

    @Test
    void "validate since value invalid"() {
        delta.since = "2014-07-05T00-00:00.000Z"
        assert validator.validateCadcDelta(delta) == ["since parameter is invalid"]
    }

    @Test
    void "validate since value short and invalid"() {
        delta.since = "2014-07-05T00:00:00"
        assert validator.validateCadcDelta(delta) == ["since parameter is invalid"]
    }

    @Test
    void "validate locale null"() {
        delta.locale = null
        assert validator.validateCadcDelta(delta) == ["locale parameter is invalid"]
    }

    @Test
    void "validate locale empty"() {
        delta.locale = ""
        assert validator.validateCadcDelta(delta) == ["locale parameter is invalid"]
    }

    @Test
    void "validate locale invalid"() {
        delta.locale = "tr_T"
        assert validator.validateCadcDelta(delta) == ["locale parameter is invalid"]
    }

    @Test
    void "validate delta type null"() {
        delta.type = null
        assert validator.validateCadcDelta(delta) == ["type parameter is invalid"]
    }

    @Test
    void "validate publication null"() {
        delta.publication = null
        assert validator.validateCadcDelta(delta) == ["publication parameter is invalid"]
    }

    @Test
    void "validate publication empty"() {
        delta.publication = ""
        assert validator.validateCadcDelta(delta) == ["publication parameter is invalid"]
    }

    @Test
    void "validate publication invalid"() {
        delta.publication = "?aa"
        assert validator.validateCadcDelta(delta) == ["publication parameter is invalid"]
    }

    @Test
    void "validate publication with dash"() {
        delta.publication = "SCORE-EDITORIAL"
        assert !validator.validateCadcDelta(delta)
    }

    @Test
    void "validate publication with underscore"() {
        delta.publication = "SCORE_EDITORIAL"
        assert !validator.validateCadcDelta(delta)
    }

    @Test
    void "validate publication alphanumeric"() {
        delta.publication = "SONY1"
        assert !validator.validateCadcDelta(delta)
    }

    @Test
    void "validate delta item type null"() {
        deltaItem.type = null
        assert validator.validateDeltaItem(deltaItem) == ["type parameter is invalid"]
    }

    @Test
    void "validate delta item"() {
        assert !validator.validateDeltaItem(deltaItem)
    }

    @Test
    void "validate delta item invalid url"() {
        deltaItem.url = "/a"
        assert validator.validateDeltaItem(deltaItem) == ["url parameter is invalid"]
    }

    @Test
    void "validate delta item invalid publication"() {
        deltaItem.publication = null
        assert validator.validateDeltaItem(deltaItem) == ["publication parameter is invalid"]
    }

    @Test
    void "validate delta item invalid locale"() {
        deltaItem.locale = null
        assert validator.validateDeltaItem(deltaItem) == ["locale parameter is invalid"]
    }

    @Test
    void "validate deltaRepo"() {
        assert !validator.validateDeltaRepo(deltaRepo)
    }

    @Test
    void "validate deltaRepo type null"() {
        deltaRepo.with {
            type = null
        }
        assert validator.validateDeltaRepo(deltaRepo) == ["type parameter is invalid"]
    }

    @Test
    void "validate deltaRepo valid sdate "() {
        deltaRepo.with {
            sdate = "2014-07-09T00:00:00.000Z"
        }
        assert !validator.validateDeltaRepo(deltaRepo)
    }

    @Test
    void "validate deltaRepo invalid sdate "() {
        deltaRepo.with {
            sdate = "s1"
        }
        assert validator.validateDeltaRepo(deltaRepo) == ["sdate parameter is invalid"]
    }

    @Test
    void "validate deltaRepo valid edate "() {
        deltaRepo.with {
            edate = "2014-07-09T00:00:00.000Z"
        }
        assert !validator.validateDeltaRepo(deltaRepo)
    }

    @Test
    void "validate deltaRepo invalid edate "() {
        deltaRepo.with {
            edate = "s2"
        }
        assert validator.validateDeltaRepo(deltaRepo) == ["edate parameter is invalid"]
    }

    @Test
    void "validate repo delta item"() {
        assert !validator.validateDeltaRepoItem(deltaRepoItem)
    }

    @Test
    void "validate repo delta item type null"() {
        deltaRepoItem.type = null
        assert validator.validateDeltaRepoItem(deltaRepoItem) == ["type parameter is invalid"]
    }

    @Test
    void "validate repo delta item invalid publication"() {
        deltaRepoItem.publication = null
        assert validator.validateDeltaRepoItem(deltaRepoItem) == ["publication parameter is invalid"]
    }

    @Test
    void "validate repo delta item invalid locale"() {
        deltaRepoItem.locale = null
        assert validator.validateDeltaRepoItem(deltaRepoItem) == ["locale parameter is invalid"]
    }

    @Test
    void "validate repo delta item invalid materialName"() {
        deltaRepoItem.materialName = null
        assert validator.validateDeltaRepoItem(deltaRepoItem) == ["sku parameter is invalid"]
    }

}
