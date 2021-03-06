package com.hornmicro.selenium.driver.api

import groovy.transform.ToString;

@ToString(includeNames=true)
class AssertResult extends Result {
    Boolean passed = true
    Boolean failed = false
    String failureMessage = ""
    
    AssertResult() {
    }
    
    void setFailed(String message) {
        this.passed = false
        this.failed = true
        this.failureMessage = message
    }
}
