package com.hornmicro.selenium.driver.api

import com.thoughtworks.selenium.Selenium

class AssertHandler extends CommandHandler {
    FunctionBind assertBlock
    Closure closure
    
    AssertHandler(FunctionBind assertBlock, Boolean haltOnFailure=null) {
        super("assert", haltOnFailure ?: false)
        this.assertBlock = assertBlock
    }
    
    AssertHandler(Closure closure, Boolean haltOnFailure=null) {
        super("assert", haltOnFailure ?: false)
        this.closure = closure
    }
    
    
    
    AssertResult execute(Selenium seleniumApi, SeleniumCommand command) {
        def result = new AssertResult()
        def callable = this.assertBlock ?: this.closure
        try {
            if(command.target && command.value) {
                callable.call(command.target, command.value)
            } else if (command.target) {
                println "Calling with '$command.target'"
                callable.call(command.target)
            } else {
                callable.call()
            }
        } catch(AssertionFailedError e) {
            if(this.haltOnFailure) {
                throw new SeleniumError(e)
            }
            result.setFailed(e.message)   
        }
        return result
    }
}
