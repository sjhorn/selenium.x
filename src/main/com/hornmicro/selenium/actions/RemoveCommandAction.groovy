package com.hornmicro.selenium.actions

import org.eclipse.jface.action.Action
import org.eclipse.swt.SWT

import com.hornmicro.selenium.model.TestCaseModel
import com.hornmicro.selenium.model.TestModel
import com.hornmicro.selenium.ui.MainController

class RemoveCommandAction extends Action {
    MainController controller 
    
    RemoveCommandAction(MainController controller) {
        super("Remove Selected Command")
        setAccelerator(SWT.MOD1 + (int)'-' )
        setToolTipText("Remove Selected Command")
        this.controller = controller    
    }
    
    void run() {
        TestCaseModel testCase = controller.model?.selectedTestCase
        TestModel testModel = testCase?.selectedTest
        if(testModel) {
            int index = testCase.tests.indexOf(testModel) - 1
            if(index < 0) {
                index = 0
            }
            testCase.tests -= testModel
            testCase.selectedTest = testCase.tests[index]  
        }
    }    
}
