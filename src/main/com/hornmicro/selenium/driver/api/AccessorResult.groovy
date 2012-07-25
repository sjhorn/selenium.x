package com.hornmicro.selenium.driver.api

import groovy.transform.ToString;

@ToString
class AccessorResult extends Result {
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