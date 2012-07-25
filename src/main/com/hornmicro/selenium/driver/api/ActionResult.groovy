package com.hornmicro.selenium.driver.api

import groovy.transform.ToString

@ToString
class ActionResult extends Result {
    
    ActionResult(terminationCondition) {
        this.terminationCondition = terminationCondition
    }
}
