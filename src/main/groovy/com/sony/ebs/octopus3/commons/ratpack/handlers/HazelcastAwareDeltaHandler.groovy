package com.sony.ebs.octopus3.commons.ratpack.handlers

import com.hazelcast.core.HazelcastInstance
import com.sony.ebs.octopus3.commons.flows.Delta
import com.sony.ebs.octopus3.commons.flows.FlowTypeEnum
import com.sony.ebs.octopus3.commons.flows.ServiceTypeEnum
import com.sony.ebs.octopus3.commons.process.ProcessIdImpl
import com.sony.ebs.octopus3.commons.ratpack.file.ResponseStorage
import com.sony.ebs.octopus3.commons.ratpack.product.cadc.delta.model.DeltaResult
import com.sony.ebs.octopus3.commons.ratpack.product.cadc.delta.service.DeltaResultService
import groovy.json.JsonOutput
import groovy.util.logging.Slf4j
import org.joda.time.DateTime
import org.springframework.beans.factory.annotation.Autowired
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

    @Autowired
    HazelcastInstance hazelcastInstance

    @Autowired
    ResponseStorage responseStorage

    @Autowired
    DeltaResultService deltaResultService

    void finalizeInAsyncThread(delta) {
        ongoingProcesses.remove delta.ticket
        log.info "Process {} is removed from ongoing processes. Remaining processes are {}", delta, ongoingProcesses
    }

    /**
     * Handle Hazelcast Delta specific operation
     */
    @Override
    final void handle(GroovyContext context) {
        //TODO: Reformat URL in context to derive below parameters from URL
        D delta = createDelta(new ProcessIdImpl(context.request.queryParams['pid']), getFlow(), ServiceTypeEnum.DELTA, getPublication(context), getLocale(context))
        activity.info "Starting delta feed generation for {}", delta

        //Validate context according to its flow; continue if there is no error
        def errors = flowValidate(context, delta)
        if (!errors) {
            ongoingProcesses = populateOngoingProcesses()

            if (delta.ticket in ongoingProcesses) {
                log.warn "Duplicate request ignored for delta {}", delta
                activity.warn "{} request is ignored because there's already an ongoing delta for {}-{}", delta, delta.publication, delta.locale
                delta.status = 400
                errors << ["Duplicate request ignored for delta"]

                def jsonResponse = deltaResultService.createDeltaResultInvalid(delta, errors)
                storeResponse(delta, jsonResponse)

                context.response.status 400
                context.render jsonResponse
            } else {
                ongoingProcesses << delta.ticket
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
        def urnValues = [delta.flow?.toString()?.toLowerCase(), delta.service?.toString()?.toLowerCase(), delta.publication, delta.locale, delta.processId?.id]
        responseStorage.store(delta.processId?.id, urnValues, JsonOutput.toJson(jsonRender.object))
    }

    JsonRender processError(Delta delta, List<String> errors, DateTime startTime) {
        finalizeInAsyncThread(delta)

        activity.error "finished {} with errors: {}", delta, errors
        def endTime = new DateTime()
        delta.status = 500

        def jsonResponse = deltaResultService.createDeltaResultWithErrors(delta, errors, startTime, endTime)
        storeResponse(delta, jsonResponse)
        jsonResponse
    }

    JsonRender processSuccess(Delta delta, DeltaResult deltaResult, DateTime startTime) {
        finalizeInAsyncThread(delta)

        activity.info "finished {} with success", delta
        def endTime = new DateTime()
        delta.status = 200

        def jsonResponse = deltaResultService.createDeltaResult(delta, deltaResult, startTime, endTime)
        storeResponse(delta, jsonResponse)
        jsonResponse
    }

    /**
     * If ongoingProcesses is null and hazelcast service is up; gets hazelcast data;
     * else returns a new list
     */
    final def populateOngoingProcesses() {
        if (!ongoingProcesses) {
            ongoingProcesses = hazelcastInstance ? hazelcastInstance.getList("ongoingProcesses") : new CopyOnWriteArrayList<Set>()
        }
    }

    /**
     * Gets flow type of service
     * @return
     */
    abstract FlowTypeEnum getFlow()

    /**
     * Gets publication from request
     */
    abstract String getPublication(GroovyContext context)

    /**
     * Gets locale from request
     */
    abstract String getLocale(GroovyContext context)

    /**
     * Validates request
     * @return list of errors
     */
    abstract List flowValidate(GroovyContext context, D delta)

    /**
     * Handle flow specific operations
     */
    abstract void flowHandle(GroovyContext context, D delta)

    /**
     * Creates a delta object of type D
     */
    abstract D createDelta(ProcessIdImpl processId, FlowTypeEnum flowTypeEnum, ServiceTypeEnum serviceTypeEnum, String publication, String locale)
}
