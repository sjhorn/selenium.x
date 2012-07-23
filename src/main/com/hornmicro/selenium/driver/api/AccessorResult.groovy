package com.hornmicro.selenium.driver.api

class AccessorResult {
    def terminationCondition
    def result
    
    AccessorResult(result) {
        if (result?.terminationCondition) {
            def self = this
            this.terminationCondition = { ->
                return result.terminationCondition(self)
            }
        } else {
            this.result = result
        }
    }
}