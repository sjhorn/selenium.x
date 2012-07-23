package com.hornmicro.selenium.driver.api

import com.thoughtworks.selenium.Selenium;

class AccessorHandler extends CommandHandler {
    FunctionBind accessBlock
    
    AccessorHandler(accessBlock) {
        super("accessor", true)
        this.accessBlock = accessBlock
    }
    
    AccessorResult execute(Selenium seleniumApi,SeleniumCommand command) {
        def returnValue = this.accessBlock(command.target, command.value)
        return new AccessorResult(returnValue)
    }
}
