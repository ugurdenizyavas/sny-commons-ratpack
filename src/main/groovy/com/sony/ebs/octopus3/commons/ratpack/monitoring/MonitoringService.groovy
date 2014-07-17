package com.sony.ebs.octopus3.commons.ratpack.monitoring

import org.springframework.stereotype.Component

@Component
class MonitoringService {

    boolean appStatus = true

    boolean checkStatus() {
        appStatus
    }

    def down() {
        appStatus = false
    }

    def up() {
        appStatus = true
    }
}
