package com.hornmicro.selenium.actions

import groovy.transform.CompileStatic

import org.eclipse.jface.action.Action
import org.eclipse.swt.SWT

import com.hornmicro.selenium.model.TestSuiteModel
import com.hornmicro.selenium.ui.MainController


class PauseAction extends Action {
    MainController controller
    TestSuiteModel model
    
    PauseAction(MainController controller) {
        super("Pause/Resume Current Test")
        setAccelerator(SWT.MOD1 + (int)'2' )
        setToolTipText("Pause/Resume Current Test")
        
        this.controller = controller
        model = controller.model
    }
    
    void run() {
        
    }
}
