package com.sony.ebs.octopus3.commons.ratpack.product.cadc.delta.service

import com.sony.ebs.octopus3.commons.ratpack.product.cadc.delta.model.DeltaResult
import com.sony.ebs.octopus3.commons.ratpack.product.cadc.delta.model.ProductResult
import org.joda.time.DateTime
import org.junit.Before
import org.junit.Test

class DeltaResultServiceTest {

    DeltaResultService deltaResultService
    def product, delta
    List<String> errors
    DateTime startTime, endTime

    @Before
    void before() {
        deltaResultService = new DeltaResultService()
        product = [a: 1, b: 2]
        delta = [:]
        errors = ["err1", "err2"]
        startTime = new DateTime()
        endTime = new DateTime()
    }

    @Test
    void "createProductResultInvalid"() {
        def ren = deltaResultService.createProductResultInvalid(product, errors)?.object
        assert ren.status == 400
        assert ren.product == product
        assert ren.errors == errors
    }

    @Test
    void "createProductResult with errors"() {
        def errors = ["err1", "err2"]
        def result = new ProductResult(inputUrn: "urn:a:b", errors: errors)
        def ren = deltaResultService.createProductResult(product, result, startTime, endTime)?.object
        assert ren.status == 500
        assert ren.product == product
        assert ren.errors == errors
        assert ren.timeStats
        assert ren.result.inputUrn == result.inputUrn
        assert ren.result.statusCode == 500
        assert ren.result.success == false
    }

    @Test
    void "createProductResult"() {
        def result = new ProductResult(inputUrn: "urn:a:b")
        def ren = deltaResultService.createProductResult(product, result, startTime, endTime)?.object
        assert ren.status == 200
        assert ren.product == product
        assert ren.result.inputUrn == result.inputUrn
        assert ren.result.statusCode == 200
        assert ren.result.success == true
        assert !ren.errors
        assert ren.timeStats
    }

    @Test
    void "createDeltaResultInvalid"() {
        def ren = deltaResultService.createDeltaResultInvalid(delta, errors)?.object
        assert ren.status == 400
        assert ren.delta == delta
        assert ren.errors == errors
    }

    @Test
    void "createDeltaResultWithErrors"() {
        def ren = deltaResultService.createDeltaResultWithErrors(delta, errors, startTime, endTime)?.object
        assert ren.status == 500
        assert ren.delta == delta
        assert ren.errors == errors
        assert ren.timeStats
    }

    @Test
    void "createDeltaResult"() {
        def deltaResult = new DeltaResult(
                deltaUrns: ["a", "b", "c", "d", "e", "f", "g", "h", "j", "k"],
                categoryFilteredOutUrns: ["a", "b"],
                eanCodeFilteredOutUrns: ["c", "d", "e"],
                successfulUrns: ["f", "g", "h"],
                unsuccessfulUrns: ["j", "k"],
                productErrors: [a: 1],
                other: [b: 2]
        )

        def ren = deltaResultService.createDeltaResult(delta, deltaResult, startTime, endTime)?.object
        assert ren.status == 200
        assert ren.delta == delta
        assert !ren.errors
        assert ren.timeStats

        assert ren.result.stats."number of delta products" == 10
        assert ren.result.stats."number of products filtered out by category" == 2
        assert ren.result.stats."number of products filtered out by ean code" == 3
        assert ren.result.stats."number of successful" == 3
        assert ren.result.stats."number of unsuccessful" == 2
        assert ren.result.stats."sum" == 10

        assert ren.result.urns.deltaUrns == ["a", "b", "c", "d", "e", "f", "g", "h", "j", "k"]
        assert ren.result.urns.categoryFilteredOutUrns == ["a", "b"]
        assert ren.result.urns.eanCodeFilteredOutUrns == ["c", "d", "e"]
        assert ren.result.urns.successfulUrns == ["f", "g", "h"]
        assert ren.result.urns.unsuccessfulUrns == ["j", "k"]

        assert ren.result.productErrors == [a: 1]
        assert ren.result.other == [b: 2]
    }
}
