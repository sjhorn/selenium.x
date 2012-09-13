package com.hornmicro.selenium.actions

import org.eclipse.jface.action.Action
import org.eclipse.swt.SWT

import com.hornmicro.selenium.model.TestCaseModel
import com.hornmicro.selenium.model.TestModel
import com.hornmicro.selenium.ui.MainController

class InsertCommandAction extends Action {
    MainController controller
    
    InsertCommandAction(MainController controller) {
        super("Insert Command")
        setAccelerator(SWT.MOD1 + (int)'i' )
        setToolTipText("Insert Command")
        this.controller = controller
    }
    
    void run() {
        TestCaseModel testCase = controller.model?.selectedTestCase
        TestModel testModel = testCase?.selectedTest
        if(testModel) {
            int index = testCase.tests.indexOf(testModel)
            if(index < 0) {
                index = 0
            }
            testCase.tests.add(index, new TestModel(command:"", target:"", value:""))
            controller.view.testCaseViewer.refresh()
            
            testCase.selectedTest = testCase.tests[index]
        }
    }
}
