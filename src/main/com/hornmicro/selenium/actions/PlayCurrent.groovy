package com.hornmicro.selenium.actions

import org.eclipse.jface.action.Action
import org.eclipse.swt.SWT
import org.eclipse.swt.graphics.Color
import org.eclipse.swt.graphics.RGB
import org.eclipse.swt.widgets.Display
import org.eclipse.swt.widgets.Table

import com.hornmicro.selenium.model.TestCaseModel
import com.hornmicro.selenium.model.TestModel
import com.hornmicro.selenium.model.TestState
import com.hornmicro.selenium.model.TestSuiteModel
import com.hornmicro.selenium.ui.MainController
import com.hornmicro.selenium.ui.Resources

class PlayCurrent extends Action {
    MainController controller
    Boolean clearProgress
    TestSuiteModel model
    
    PlayCurrent(MainController controller, Boolean clearProgress=true) {
        super("Play Current Test")
        setAccelerator(SWT.MOD1 + (int)'1' )
        setToolTipText("Play Current Test")
        
        this.controller = controller
        this.clearProgress = clearProgress
        model = controller.model
    }
    
    void run() {        
        TestCaseModel testCase = model.selectedTestCase
        if(clearProgress) {
            model.testCases.each { TestCaseModel tcm ->
                tcm.state = TestState.UNKNOWN
            }
            model.runs = 0
            model.failures = 0
            controller.view.greenBar.setBackgroundImage(Resources.getImage("gfx/progress-background.png"))
        }
        int index = model.testCases.indexOf(testCase)
        
        testCase.state = TestState.INPROGRESS
        controller.view.testCasesViewer.table.setSelection(-1)
        
        if(testCase) {
            controller.view.testCasesViewer.table.setEnabled(false)
            nextTest(testCase, true)
        }
    }
    
    void resume() {
        
    }
    
    
    private TestModel nextTest(TestCaseModel testCase, Boolean clearProgress, index=0) {
        if(index >= testCase.tests.size()) {
            println "All done - woohoo!"
            onComplete(testCase)
        } else if(testCase.tests[index]) {
            Display.default.asyncExec {
                testCase.selectedTest = testCase.tests[index]
                ExecuteAction ea = new ExecuteAction(
                    controller, 
                    clearProgress, 
                    false,
                    _onSuccess(testCase, false, index+1),
                    _onError(testCase),
                    _onCancelled(testCase)
                )
                ea.run()
            }
        }
    }
    
    private void onComplete(TestCaseModel testCase) {
        testCase.state = TestState.SUCCESS
        model.runs++
        updateGreenBar()
    }
    
    private Closure _onSuccess(TestCaseModel testCase, boolean clearProgress, int index) {
        return { ->
            nextTest(testCase, clearProgress, index)
        }
    }
    
    private Closure _onCancelled(TestCaseModel testCase) {
        return { ->
            testCase.state = TestState.UNKNOWN
            updateGreenBar()
        }
    }
    
    private Closure _onError(TestCaseModel testCase) {
        return { ->
            testCase.state = TestState.FAILED
            model.failures += 1
            updateGreenBar()
        }
    }
    
    private updateGreenBar() {
        controller.view.testCasesViewer.refresh()
        controller.view.testCasesViewer.table.setEnabled(true)
        if(model.failures > 0) {
            controller.view.greenBar.setBackgroundImage(Resources.getImage("gfx/progress-failure.png"))
        } else if(model.runs > 0) {
            controller.view.greenBar.setBackgroundImage(Resources.getImage("gfx/progress-success.png"))
        } else {
            controller.view.greenBar.setBackgroundImage(Resources.getImage("gfx/progress-background.png"))
        }
    }
}
