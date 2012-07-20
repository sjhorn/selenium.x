package com.hornmicro.selenium.actions

import org.eclipse.jface.action.Action
import org.eclipse.swt.widgets.Display

import com.hornmicro.selenium.driver.DriveTest
import com.hornmicro.selenium.model.TestCaseModel
import com.hornmicro.selenium.model.TestModel
import com.hornmicro.selenium.model.TestSuiteModel

class FindAction extends Action {
    def controller
    
    FindAction(controller) {
        this.controller = controller
    }
    
    void run() {
        def log = controller.view.log
        TestSuiteModel model = controller.model
        TestCaseModel tcm = controller.model?.selectedTestCase
        TestModel tm = tcm?.selectedTest
        if(tm) {
            Thread.start {
                if(!DriveTest.findElement(model.browser, tcm.baseURL, tm.target)) {
                    Display.default.asyncExec {
                        log.error("locator not found ${tm.target}")
                    }
                }
            }
        }
    }
}
