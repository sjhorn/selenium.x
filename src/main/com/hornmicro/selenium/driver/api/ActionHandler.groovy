package com.hornmicro.selenium.driver.api

import com.thoughtworks.selenium.Selenium;

class ActionHandler extends CommandHandler {
    Boolean wait
    Boolean checkAlerts
    FunctionBind actionBlock
    
    ActionHandler(actionBlock, wait, dontCheckAlerts) {
        super("action", true)

        this.actionBlock = actionBlock
        if (wait) {
            this.wait = true
        }
        
        // note that dontCheckAlerts could be undefined!!!
        this.checkAlerts = (dontCheckAlerts) ? false : true
    }
    
    ActionResult execute(Selenium seleniumApi,SeleniumCommand command) {
        //if (this.checkAlerts && !(command.command =~ /(Alert|Confirmation)(Not)?Present/).matches()  ) {
            // todo: this conditional logic is ugly
        //    seleniumApi.ensureNoUnhandledPopups();
        //}
        
        def handlerCondition = this.actionBlock.call(command.target, command.value)
        
        // page load waiting takes precedence over any wait condition returned by
        // the action handler.
        def terminationCondition = (this.wait) ? makePageLoadCondition() : handlerCondition
        
        return new ActionResult(terminationCondition);
    }
    
    def makePageLoadCondition() {
        
    }
}
