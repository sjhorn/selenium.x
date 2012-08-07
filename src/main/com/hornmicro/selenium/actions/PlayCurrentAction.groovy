package com.hornmicro.selenium.actions

import org.eclipse.jface.action.Action
import org.eclipse.jface.resource.ImageDescriptor
import org.eclipse.swt.SWT
import org.eclipse.swt.widgets.Display

import com.hornmicro.selenium.model.RunState
import com.hornmicro.selenium.model.TestCaseModel
import com.hornmicro.selenium.model.TestState
import com.hornmicro.selenium.model.TestSuiteModel
import com.hornmicro.selenium.ui.MainController
import com.hornmicro.selenium.ui.Resources

class PlayCurrentAction extends Action {
    MainController controller
    Boolean clearProgress
    TestSuiteModel model
    Closure onComplete
    
    PlayCurrentAction(MainController controller, Boolean clearProgress=true, Closure onComplete=null) {
        super("Play current test")
        setAccelerator(SWT.MOD1 + (int)'1' )
        setToolTipText("Play current test")
        setImageDescriptor(ImageDescriptor.createFromImage(Resources.getImage("gfx/PlayOne.png")))
        
        this.controller = controller
        this.clearProgress = clearProgress
        this.onComplete = onComplete
        model = controller.model
    }
    
    void run() {        
        TestCaseModel testCase = model.selectedTestCase
        if(testCase) {
            controller.setRunning(RunState.RUNNING)
            
            if(clearProgress) {
                testCase.paused = false
                model.testCases.each { TestCaseModel tcm ->
                    tcm.state = TestState.UNKNOWN
                }
                model.runs = 0
                model.failures = 0
                controller.view.greenBar.setBackgroundImage(Resources.getImage("gfx/progress-background.png"))
                controller.pauseResumeAction.reset()
            }
            int index = model.testCases.indexOf(testCase)
            
            testCase.state = TestState.INPROGRESS
            controller.view.testCasesViewer.table.setSelection(-1)
            controller.view.testCasesViewer.table.setEnabled(false)
            nextTest(testCase, true)
        }
    }
    
    void resume() {
        TestCaseModel testCase = model.selectedTestCase
        if(testCase) {
            if(!testCase.paused) {
                return
            }
            testCase.paused = false
            controller.setRunning(RunState.RUNNING)
            
            testCase.state = TestState.INPROGRESS
            controller.view.testCasesViewer.table.setSelection(-1)
            controller.view.testCasesViewer.table.setEnabled(false)
            nextTest(testCase, false, testCase.currentTest)
        }
    }
    
    private void nextTest(TestCaseModel testCase, Boolean clearProgress, int index=0) {
        if(index >= testCase.tests.size()) {
            println "All done - woohoo!"
            onComplete(testCase)
        } else if(testCase.paused) {
            onPause()
        } else if(testCase.tests[index]) {
            Display.default.asyncExec {
                testCase.currentTest = index
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
    
    private void onPause() {
        controller.view.testCasesViewer.table.setEnabled(true)
        controller.setRunning(RunState.PAUSED)
    }
    
    private void onComplete(TestCaseModel testCase) {
        testCase.state = TestState.SUCCESS
        model.runs++
        updateGreenBar()
        onComplete?.call()
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
        controller.setRunning(RunState.STOPPED)
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
