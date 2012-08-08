package com.hornmicro.selenium.actions

import org.eclipse.jface.action.Action
import org.eclipse.jface.resource.ImageDescriptor
import org.eclipse.swt.SWT

import com.hornmicro.selenium.model.TestCaseModel
import com.hornmicro.selenium.model.TestSuiteModel
import com.hornmicro.selenium.ui.MainController
import com.hornmicro.selenium.ui.Resources


class PauseResumeAction extends Action {
    MainController controller
    TestSuiteModel model
    
    PauseResumeAction(MainController controller) {
        super("Pause/Resume Current Test")
        setAccelerator(SWT.MOD1 + (int)'3' )
        setToolTipText("Pause/Resume Current Test")
        setImageDescriptor(ImageDescriptor.createFromImage(Resources.getImage("gfx/Pause.png")))
        this.controller = controller
        model = controller.model
    }
    
    void run() {
        TestCaseModel testCase = model.selectedTestCase
        if(testCase.paused) {
            controller.playCurrentAction.resume()
            setImageDescriptor(ImageDescriptor.createFromImage(Resources.getImage("gfx/Pause.png")))
        } else {
            testCase.paused = true
            setImageDescriptor(ImageDescriptor.createFromImage(Resources.getImage("gfx/Continue.png")))
        }
    }
    
    void reset() {
        setImageDescriptor(ImageDescriptor.createFromImage(Resources.getImage("gfx/Pause.png")))
    }
}
