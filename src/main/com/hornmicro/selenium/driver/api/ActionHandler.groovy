package com.hornmicro.selenium.driver.api

import com.thoughtworks.selenium.Selenium;

class ActionHandler extends CommandHandler {
    Boolean wait
    Boolean checkAlerts
    FunctionBind actionBlock
    
    Closure closure
    
    ActionHandler(FunctionBind actionBlock, Boolean wait, Boolean dontCheckAlerts) {
        super("action", true)

        this.actionBlock = actionBlock
        if (wait) {
            this.wait = true
        }
        
        // note that dontCheckAlerts could be undefined!!!
        this.checkAlerts = (dontCheckAlerts) ? false : true
    }
    
    ActionHandler(Closure closure, Boolean wait, Boolean dontCheckAlerts) {
        super("action", true)

        this.closure = closure
        if (wait) {
            this.wait = true
        }
        
        // note that dontCheckAlerts could be undefined!!!
        this.checkAlerts = (dontCheckAlerts) ? false : true
    }
    
    
    
    Result execute(Selenium selenium, SeleniumCommand command) {
        //if (this.checkAlerts && !(command.command =~ /(Alert|Confirmation)(Not)?Present/).matches()  ) {
            // todo: this conditional logic is ugly
        //    seleniumApi.ensureNoUnhandledPopups();
        //}
        def callable = this.actionBlock ?: this.closure
        
        def handlerCondition
        if(command.target && command.value) {
            handlerCondition = callable.call(command.target, command.value)
        } else if (command.target) {
            handlerCondition = callable.call(command.target)
        } else {
            handlerCondition = callable.call()
        }
        //def handlerCondition = callable.call(command.target, command.value)
        
        // page load waiting takes precedence over any wait condition returned by
        // the action handler.
        def terminationCondition = (this.wait) ? makePageLoadCondition(selenium) : handlerCondition
        
        return new ActionResult(terminationCondition);
    }
    
    def makePageLoadCondition(Selenium selenium) {
        Long timeoutTime = new Date().getTime() + CommandHandlerFactory.DEFAULT_TIMEOUT
        return { ->
            if (new Date().getTime() > timeoutTime) {
                //if (callback != null) {
                //     callback();
                //}
                throw new SeleniumError("Timed out after " + CommandHandlerFactory.DEFAULT_TIMEOUT + "ms")
            }
            try {
                selenium.waitForPageToLoad(1) // cheap a nasty polling with 1ms timeout
                return true
            } catch(e) {
                return false
            }
        }
    }

}
