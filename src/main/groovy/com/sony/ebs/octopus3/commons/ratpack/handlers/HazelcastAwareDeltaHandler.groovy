package com.sony.ebs.octopus3.commons.ratpack.handlers

import com.hazelcast.core.HazelcastInstance
import com.sony.ebs.octopus3.commons.flows.Delta
import com.sony.ebs.octopus3.commons.flows.FlowTypeEnum
import com.sony.ebs.octopus3.commons.flows.RepoValue
import com.sony.ebs.octopus3.commons.flows.ServiceTypeEnum
import com.sony.ebs.octopus3.commons.process.ProcessIdImpl
import com.sony.ebs.octopus3.commons.ratpack.file.ResponseStorage
import com.sony.ebs.octopus3.commons.ratpack.product.cadc.delta.model.DeltaResult
import com.sony.ebs.octopus3.commons.ratpack.product.cadc.delta.service.DeltaResultService
import com.sony.ebs.octopus3.commons.ratpack.product.cadc.delta.validator.RequestValidator
import groovy.json.JsonOutput
import groovy.util.logging.Slf4j
import org.joda.time.DateTime
import ratpack.groovy.handling.GroovyContext
import ratpack.groovy.handling.GroovyHandler
import ratpack.jackson.JsonRender

import java.util.concurrent.CopyOnWriteArrayList

/**
 * author: TRYavasU
 * date: 20/10/2014
 */
@Slf4j(value = "log", category = "DeltaHandler")
@Slf4j(value = "activity", category = "activity")
abstract class HazelcastAwareDeltaHandler<D extends Delta> extends GroovyHandler {

    def ongoingProcesses

    HazelcastInstance hazelcastInstance
    ResponseStorage responseStorage
    DeltaResultService deltaResultService
    RequestValidator validator

    abstract RepoValue getDeltaType()

    abstract FlowTypeEnum getFlowType()

    abstract D createDelta(GroovyContext context)

    abstract void flowHandle(GroovyContext context, D delta)

    List flowValidate(GroovyContext context, D delta) {
        validator.validateDelta(delta)
    }

    void finalizeInAsyncThread(delta) {
        if (ongoingProcesses != null) {
            ongoingProcesses.remove delta.ticket
            log.info "Process {} is removed from ongoing processes. Remaining processes are {}", delta, ongoingProcesses
        }
    }

    /**
     * Handle Hazelcast Delta specific operation
     */
    @Override
    final void handle(GroovyContext context) {
        D delta = createDelta(context)
        delta.with {
            processId = new ProcessIdImpl(context.request.queryParams.pid)
            flow = flowType
            service = ServiceTypeEnum.DELTA
            type = deltaType
            publication = context.pathTokens.publication
            locale = context.pathTokens.locale
            upload = context.request.queryParams.upload as boolean
            sdate = context.request.queryParams.sdate
            edate = context.request.queryParams.edate
        }

        activity.info "Starting delta feed generation for {}", delta

        //Validate context according to its flow; continue if there is no error
        def errors = flowValidate(context, delta)
        if (!errors) {
            ongoingProcesses = populateOngoingProcesses()

            if (ongoingProcesses != null && delta.ticket in ongoingProcesses) {
                log.warn "Duplicate request ignored for delta {}", delta
                activity.warn "{} request is ignored because there's already an ongoing delta for {}-{}", delta, delta.publication, delta.locale
                delta.status = 400
                errors << ["Duplicate request ignored for delta"]

                def jsonResponse = deltaResultService.createDeltaResultInvalid(delta, errors)
                storeResponse(delta, jsonResponse)

                context.response.status 400
                context.render jsonResponse
            } else {
                if (ongoingProcesses != null) {
                    ongoingProcesses << delta.ticket
                }
                flowHandle context, delta
            }
        } else {
            log.warn "Validation fails for {}", delta
            activity.error "{} request is rejected because validation fails", delta
            delta.status = 400

            def jsonResponse = deltaResultService.createDeltaResultInvalid(delta, errors)
            storeResponse(delta, jsonResponse)

            context.response.status 400
            context.render jsonResponse
        }

    }

    void storeResponse(Delta delta, JsonRender jsonRender) {
        responseStorage.store(delta, JsonOutput.toJson(jsonRender.object))
    }

    JsonRender processResult(Delta delta, DeltaResult deltaResult, DateTime startTime) {
        finalizeInAsyncThread(delta)

        def endTime = new DateTime()
        if (deltaResult.errors) {
            activity.error "finished {} with errors: {}", delta, deltaResult.errors
            delta.status = 500
        } else {
            activity.info "finished {} with success", delta
            delta.status = 200
        }

        def jsonResponse = deltaResultService.createDeltaResult(delta, deltaResult, startTime, endTime)
        storeResponse(delta, jsonResponse)
        jsonResponse
    }

    /**
     * If ongoingProcesses is null and hazelcast service is up; gets hazelcast data;
     * else returns a new list
     */
    final def populateOngoingProcesses() {
        if (hazelcastInstance && !ongoingProcesses) {
            ongoingProcesses = hazelcastInstance?.getList("ongoingProcesses")
        }
    }

}
