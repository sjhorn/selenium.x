package com.hornmicro.selenium.actions

import org.eclipse.jface.action.Action
import org.eclipse.swt.SWT

import com.hornmicro.selenium.model.TestSuiteModel

class RemoveTestCaseAction extends Action {
    def controller 
    
    RemoveTestCaseAction(controller) {
        super("Remove Selected Test Case")
        setAccelerator(SWT.MOD1 + SWT.SHIFT + (int)'-' )
        setToolTipText("Remove Selected Test Case")
        
        this.controller = controller    
    }
    
    void run() {
        TestSuiteModel model = controller.model
        if(model.selectedTestCase) {
            int index = model.testCases.indexOf(model.selectedTestCase) - 1
            if(index < 0) {
                index = 0
            }
            model.testCases -= model.selectedTestCase
            model.selectedTestCase = model.testCases[index]  
        }
    }    
}
