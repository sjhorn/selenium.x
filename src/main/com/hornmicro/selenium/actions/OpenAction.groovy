package com.hornmicro.selenium.actions

import org.eclipse.jface.action.Action
import org.eclipse.jface.dialogs.MessageDialog
import org.eclipse.swt.SWT
import org.eclipse.swt.widgets.FileDialog

import com.hornmicro.selenium.model.Status
import com.hornmicro.selenium.model.TestSuiteModel

class OpenAction extends Action {
    def controller
    
    OpenAction(controller) {
        super("&Open")
        setAccelerator(SWT.MOD1 + (int)'O' )
        setToolTipText("Open")
        
        this.controller = controller
    }
    
    void run() {
        TestSuiteModel model = controller.model
        def shell = controller.shell

        FileDialog dlg = new FileDialog(shell, SWT.OPEN)
        dlg.filterNames = ["All Files (*.*)"]
        dlg.filterExtensions = ["*.*"]
        String fileName = dlg.open()
        
        if (fileName != null /*&& checkOverwrite(model, shell) */ ) {
            Status status = model.open(new File(fileName))
            if(!status.success) {
                MessageDialog.openError(shell, "Open Failed", status.message)
            }
        }
        
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
