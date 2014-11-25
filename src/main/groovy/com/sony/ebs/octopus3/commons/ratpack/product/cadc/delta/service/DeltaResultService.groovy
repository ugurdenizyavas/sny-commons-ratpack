package com.sony.ebs.octopus3.commons.ratpack.product.cadc.delta.service

import com.sony.ebs.octopus3.commons.ratpack.handlers.HandlerUtil
import com.sony.ebs.octopus3.commons.ratpack.product.cadc.delta.model.DeltaResult
import com.sony.ebs.octopus3.commons.ratpack.product.cadc.delta.model.ProductResult
import org.joda.time.DateTime
import ratpack.jackson.JsonRender

import static ratpack.jackson.Jackson.json

class DeltaResultService {

    JsonRender createProductResultInvalid(Object product, List<String> errors) {
        json(status: 400, errors: errors, product: product)
    }

    JsonRender createProductResult(Object product, ProductResult result, DateTime startTime, DateTime endTime) {
        def timeStats = HandlerUtil.getTimeStats(startTime, endTime)
        if (result.errors) {
            result.success = false
            result.statusCode = 500
            json(status: 500, timeStats: timeStats, errors: result.errors, result: result, product: product)
        } else {
            result.success = true
            result.statusCode = 200
            json(status: 200, timeStats: timeStats, result: result, product: product)
        }
    }

    JsonRender createDeltaResultInvalid(Object delta, List<String> errors) {
        json(status: 400, errors: errors, delta: delta)
    }

    JsonRender createDeltaResult(Object delta, DeltaResult result, DateTime startTime, DateTime endTime) {
        def timeStats = HandlerUtil.getTimeStats(startTime, endTime)
        if (result.errors) {
            def resultMap = [
                    finalStartDate: result.finalStartDate,
                    finalDeltaUrl : result.finalDeltaUrl
            ]
            json(status: 500, timeStats: timeStats, errors: result.errors, result: resultMap, delta: delta)
        } else {
            int sum = (result.categoryFilteredOutUrns?.size() ?: 0) + (result.eanCodeFilteredOutUrns?.size() ?: 0) + (result.successfulUrns?.size() ?: 0) + (result.unsuccessfulUrns?.size() ?: 0)
            def resultMap = [
                    productErrors : result.productErrors,
                    stats         : [
                            "number of delta products"                   : result.deltaUrns?.size(),
                            "number of products filtered out by category": result.categoryFilteredOutUrns?.size(),
                            "number of products filtered out by ean code": result.eanCodeFilteredOutUrns?.size(),
                            "number of successful"                       : result.successfulUrns?.size(),
                            "number of unsuccessful"                     : result.unsuccessfulUrns?.size(),
                            "sum"                                        : sum
                    ],
                    urns          : [
                            deltaUrns              : result.deltaUrns,
                            categoryFilteredOutUrns: result.categoryFilteredOutUrns,
                            eanCodeFilteredOutUrns : result.eanCodeFilteredOutUrns,
                            successfulUrns         : result.successfulUrns,
                            unsuccessfulUrns       : result.unsuccessfulUrns
                    ],
                    other         : result.other,
                    finalStartDate: result.finalStartDate,
                    finalDeltaUrl : result.finalDeltaUrl
            ]
            json(status: 200, timeStats: timeStats, result: resultMap, delta: delta)
        }
    }

}
