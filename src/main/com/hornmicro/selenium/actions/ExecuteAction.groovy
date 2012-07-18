package com.hornmicro.selenium.actions

import org.eclipse.jface.action.Action

import com.hornmicro.selenium.model.TestModel

class ExecuteAction extends Action {
    def controller
    
    ExecuteAction(controller) {
        this.controller = controller
    }
    
    void run() {
        TestModel tm = controller.model?.selectedTestCase?.selectedTest
        if(tm) {
            DriveTest.executeAction(tm.command, tm.target, tm.value)
        }
    }
}
