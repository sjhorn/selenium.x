package com.hornmicro.selenium.actions

import org.eclipse.jface.action.Action
import org.eclipse.swt.SWT

import com.hornmicro.selenium.driver.DriveTest

class ResetBrowsersAction extends Action {

    ResetBrowsersAction() {
        super("Reset/close browser instances")
        setAccelerator(SWT.F5 )
        setToolTipText("Reset/close browser instances")
    }
    
    void run() {
        DriveTest.dispose()
    }
    
}
