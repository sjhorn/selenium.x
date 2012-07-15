package com.hornmicro.selenium.actions

import org.eclipse.jface.action.Action
import org.eclipse.swt.SWT

class ReloadAction extends Action {
    def controller
    
    ReloadAction(controller) {
        super("&Reload")
        setAccelerator(SWT.MOD1 + (int)'R' )
        setToolTipText("Reload")
        
        this.controller = controller
    }
    
    void run() {
        controller.reload()
    }
    
}
