package com.hornmicro.selenium.driver.api

import groovy.transform.ToString

import com.thoughtworks.selenium.Selenium

@ToString
abstract class CommandHandler {
    def type
    def haltOnFailure
    
    CommandHandler(type, haltOnFailure) {
        this.type = type
        this.haltOnFailure = haltOnFailure
    }
    
    abstract Result execute(Selenium selenium, SeleniumCommand command)
}
