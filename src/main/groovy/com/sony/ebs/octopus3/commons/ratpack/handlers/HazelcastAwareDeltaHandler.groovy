package com.sony.ebs.octopus3.commons.ratpack.handlers

import com.hazelcast.core.HazelcastInstance
import com.sony.ebs.octopus3.commons.flows.FlowTypeEnum
import com.sony.ebs.octopus3.commons.flows.ServiceTypeEnum
import com.sony.ebs.octopus3.commons.process.ProcessIdImpl
import com.sony.ebs.octopus3.commons.ratpack.file.ResponseStorage
import groovy.json.JsonOutput
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import ratpack.groovy.handling.GroovyContext
import ratpack.groovy.handling.GroovyHandler
import ratpack.jackson.JsonRender
import com.sony.ebs.octopus3.commons.flows.Delta

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

    @Autowired
    ResponseStorage responseStorage

    /**
     * Handle Hazelcast Delta specific operation
     */
    @Override
    final void handle(GroovyContext context) {
        //TODO: Reformat URL in context to derive below parameters from URL
        D delta = createDelta(new ProcessIdImpl(context.request.queryParams['pid']), getFlow(), ServiceTypeEnum.DELTA, getPublication(context), getLocale(context))
        activity.info "Starting delta feed generation for {}", delta

        //Validate context according to its flow
        flowValidate context, delta

        ongoingProcesses = populateOngoingProcesses()

        if (delta in ongoingProcesses) {
            log.warn "Duplicate request ignored for delta {}", delta
            activity.warn "{} request is ignored because there's already an ongoing delta for {}-{}", delta, delta.publication, delta.locale
            context.response.status 400
            delta.status = 400
            def jsonResponse = generateDeltaResponse(delta)

            responseStorage.store(delta.processId.id, [getFlow().toString().toLowerCase(), "delta", delta.publication, delta.locale, delta.processId.id], JsonOutput.toJson(jsonResponse.object))

            context.render jsonResponse
        } else {
            ongoingProcesses << delta
            //do flow specific handle operations
            flowHandle context, delta
            ongoingProcesses.remove delta
        }

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
     */
    abstract void flowValidate(GroovyContext context, D delta)

    /**
     * Handle flow specific operations
     */
    abstract void flowHandle(GroovyContext context, D delta)

    /**
     * Generate a Json response for duplicate request
     * @param delta
     * @return
     */
    //TODO: Generate a generic response
    abstract JsonRender generateDeltaResponse(D delta)

    /**
     * Creates a delta object of type D
     */
    abstract D createDelta(ProcessIdImpl processId, FlowTypeEnum flowTypeEnum, ServiceTypeEnum serviceTypeEnum, String publication, String locale)
}
