package com.hornmicro.selenium.actions

import org.eclipse.jface.action.Action
import org.eclipse.swt.SWT

import com.hornmicro.selenium.model.TestSuiteModel
import com.hornmicro.selenium.ui.MainController

class NewTestSuiteAction extends Action {
    MainController controller
    
    NewTestSuiteAction(MainController controller) {
        super("New Test Suite")
        setAccelerator(SWT.MOD1 + SWT.SHIFT + (int)'N' )
        setToolTipText("New Test Suite")
        
        this.controller = controller
    }
    
    void run() {
        
        // clear instead of creating new to maintain binding with ui
        controller.model.clear()
        controller.view.testCasesViewer.refresh()
    }
}
