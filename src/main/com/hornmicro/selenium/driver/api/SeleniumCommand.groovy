package com.hornmicro.selenium.driver.api

class SeleniumCommand {
    String command
    String target
    String value
    Boolean breakpoint
    
    SeleniumCommand(String command,String target=null,String value=null,Boolean isBreakpoint=false) {
        this.command = command.trim();
        this.target = target;
        this.value = value;
        this.breakpoint = isBreakpoint;
    }
}
