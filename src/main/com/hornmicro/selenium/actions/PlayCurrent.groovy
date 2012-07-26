package com.hornmicro.selenium.actions

import org.eclipse.jface.action.Action
import org.eclipse.swt.SWT
import org.eclipse.swt.widgets.Display

import com.hornmicro.selenium.model.TestCaseModel
import com.hornmicro.selenium.model.TestModel
import com.hornmicro.selenium.model.TestSuiteModel
import com.hornmicro.selenium.ui.MainController

class PlayCurrent extends Action {
    MainController controller
    synchronized Boolean running = false
    
    PlayCurrent(controller) {
        super("Play Current Test")
        setAccelerator(SWT.MOD1 + (int)'1' )
        setToolTipText("Play Current Test")
        
        this.controller = controller
    }
    
    void run() {
        TestSuiteModel model = controller.model
        TestCaseModel testCase = model.selectedTestCase
        if(testCase && !running) {
            running = true
            nextTest(testCase, true)
        }
    }
    
    TestModel nextTest(TestCaseModel testCase, Boolean clearProgress, index=0) {
        if(index >= testCase.tests.size()) {
            running = false
            println "All done - woohoo!"
        } else if(testCase.tests[index]) {
            Display.default.asyncExec {
                testCase.selectedTest = testCase.tests[index]
                ExecuteAction ea = new ExecuteAction(controller, clearProgress, {
                    nextTest(testCase, false, index+1)
                })
                ea.run()
            }
        }
    }
}
