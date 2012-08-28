package com.hornmicro.selenium.actions

import org.eclipse.jface.action.Action
import org.eclipse.swt.SWT

class NewTestSuiteAction extends Action {
    def controller
    
    NewTestSuiteAction(controller) {
        super("New Test Suite")
        setAccelerator(SWT.MOD1 + SWT.SHIFT + (int)'N' )
        setToolTipText("New Test Suite")
        
        this.controller = controller
    }
}
