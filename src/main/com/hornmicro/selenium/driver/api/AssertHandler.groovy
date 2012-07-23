package com.hornmicro.selenium.driver.api

import com.thoughtworks.selenium.Selenium

class AssertHandler extends CommandHandler {
    FunctionBind assertBlock
    
    AssertHandler(assertBlock, haltOnFailure=null) {
        super("assert", haltOnFailure ?: false)
    }
    
    AssertResult execute(Selenium seleniumApi, SeleniumCommand command) {
        def result = new AssertResult()
        
        try {
            this.assertBlock(command.target, command.value)
        } catch(AssertionFailedError e) {
            if(this.haltOnFailure) {
                throw new SeleniumError(e)
            }
            result.setFailed(e.message)   
        }
    }
}
