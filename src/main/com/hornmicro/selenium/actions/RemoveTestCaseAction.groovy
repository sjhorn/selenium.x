package com.hornmicro.selenium.actions

import org.eclipse.jface.action.Action

import com.hornmicro.selenium.model.TestSuiteModel;

class RemoveTestCaseAction extends Action {
    def controller 
    
    RemoveTestCaseAction(controller) {
        this.controller = controller    
    }
    
    void run() {
        TestSuiteModel model = controller.model
        if(model.selectedTestCase) {
            model.testCases -= model.selectedTestCase
            model.selectedTestCase = null
        }
    }    
}
