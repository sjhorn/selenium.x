package com.hornmicro.selenium.actions

import org.eclipse.jface.action.Action
import org.eclipse.jface.resource.ImageDescriptor
import org.eclipse.swt.SWT
import org.eclipse.swt.widgets.Display

import com.hornmicro.selenium.model.TestCaseModel
import com.hornmicro.selenium.model.TestState
import com.hornmicro.selenium.model.TestSuiteModel
import com.hornmicro.selenium.ui.MainController
import com.hornmicro.selenium.ui.Resources

class PlayAllAction extends Action {
    MainController controller
    Boolean clearProgress
    TestSuiteModel model
    
    PlayAllAction(MainController controller, Boolean clearProgress=true) {
        super("Play all tests")
        setAccelerator(SWT.MOD1 + (int)'1' )
        setToolTipText("Play all tests")
        setImageDescriptor(ImageDescriptor.createFromImage(Resources.getImage("gfx/PlayAll.png")))
        
        this.controller = controller
        this.clearProgress = clearProgress
        model = controller.model
    }
    
    void run() {
        if(clearProgress) {
            model.testCases.each { TestCaseModel tcm ->
                tcm.state = TestState.UNKNOWN
                tcm.paused = false
            }
            model.runs = 0
            model.failures = 0
            controller.view.greenBar.setBackgroundImage(Resources.getImage("gfx/progress-background.png"))
            controller.pauseResumeAction.reset()
        }
        nextTest()
    }
    
    void nextTest(int index=0) {
        if(index >= model.testCases.size()) {
            onComplete()
        } else if(model.testCases[index]) {
            Display.default.asyncExec {
                model.selectedTestCase = model.testCases[index]
                model.selectedTestCase.selectedTest = model.selectedTestCase.tests[0]
                
                PlayCurrentAction pc = new PlayCurrentAction(controller, false, { -> nextTest(index+1) })
                pc.run()
            }
        }
    }
    
    void onPause() {
        
    }
    
    void onComplete() {
        println "All tests done - woohoo!"
    }
}
