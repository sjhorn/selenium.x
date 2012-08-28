package com.hornmicro.selenium.actions

import org.eclipse.jface.action.Action
import org.eclipse.jface.dialogs.MessageDialog
import org.eclipse.swt.SWT

import com.hornmicro.selenium.model.Status
import com.hornmicro.selenium.model.TestCaseModel
import com.hornmicro.selenium.model.TestSuiteModel
import com.hornmicro.selenium.ui.MainController

class SaveTestSuiteAction extends Action {
    MainController controller
    
    SaveTestSuiteAction(MainController controller) {
        super("Save Test Suite")
        setAccelerator(SWT.MOD1 + SWT.SHIFT +(int)'S' )
        setToolTipText("Save Test Suite")
        
        this.controller = controller
    }
    
    void run() {
        TestSuiteModel testSuite = controller.model
        if(testSuite) {
            new SaveTestSuiteAsAction(controller, testSuite.file).run()
        }
    }
}
