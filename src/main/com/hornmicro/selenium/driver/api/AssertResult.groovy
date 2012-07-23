package com.hornmicro.selenium.driver.api

class AssertResult {
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
