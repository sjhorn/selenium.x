package com.hornmicro.selenium.actions

import org.eclipse.jface.action.Action
import org.eclipse.swt.SWT

import com.hornmicro.selenium.model.TestCaseModel
import com.hornmicro.selenium.model.TestModel
import com.hornmicro.selenium.ui.MainController

class AddCommandAction extends Action {
    MainController controller
    
    AddCommandAction(MainController controller) {
        super("Add Command")
        setAccelerator(SWT.MOD1 + (int)'+' )
        setToolTipText("Add Command")
        this.controller = controller
    }
    
    void run() {
        TestCaseModel testCase = controller.model?.selectedTestCase
        if(testCase) {
            TestModel testModel = new TestModel(command:"", target:"", value:"")
            testCase.tests += testModel
            testCase.selectedTest = testModel
            
            //controller.view.
        }
    }
}
