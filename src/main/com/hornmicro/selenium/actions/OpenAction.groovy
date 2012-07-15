package com.hornmicro.selenium.actions

import org.eclipse.jface.action.Action
import org.eclipse.swt.SWT
import org.eclipse.swt.widgets.FileDialog

class OpenAction extends Action {
    def controller
    
    OpenAction(controller) {
        super("&Open")
        setAccelerator(SWT.MOD1 + (int)'O' )
        setToolTipText("Open")
        
        this.controller = controller
    }
    
    void run() {
//        def model = controller.activeModel
        def shell = controller.shell

        FileDialog dlg = new FileDialog(shell, SWT.OPEN)
        dlg.filterNames = ["All Files (*.*)"]
        dlg.filterExtensions = ["*.*"]
        String fileName = dlg.open()
        /*
        if (fileName != null && checkOverwrite(model, shell) ) {
            try {
                model.clear()
                model.fileName = fileName
                model.open()
            } catch (IOException e) {
                MessageDialog.openError(shell, "Error", e.message);
            }
        }
        */
    }
/*
    boolean checkOverwrite(model, shell) {
        def proceed = true;
        if (model.isDirty()) {
            proceed = MessageDialog.openConfirm(shell, "Are you sure?",
                    "You have unsaved changes, are you sure you want to lose them?");
        }
        return proceed;
    }
*/
}
