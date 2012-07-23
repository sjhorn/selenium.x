package com.hornmicro.selenium.driver.api

class CommandHandler {
    def type
    def haltOnFailure
    
    CommandHandler(type, haltOnFailure) {
        this.type = type
        this.haltOnFailure = haltOnFailure
    }
}
