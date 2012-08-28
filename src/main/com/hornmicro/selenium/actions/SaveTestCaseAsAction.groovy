package com.hornmicro.selenium.actions

import org.eclipse.jface.action.Action
import org.eclipse.jface.dialogs.MessageDialog
import org.eclipse.swt.SWT
import org.eclipse.swt.widgets.FileDialog
import org.eclipse.swt.widgets.MessageBox

import com.hornmicro.selenium.model.Status
import com.hornmicro.selenium.model.TestCaseModel
import com.hornmicro.selenium.ui.MainController

class SaveTestCaseAsAction extends Action {
    MainController controller
    TestCaseModel testCase
    Closure onSuccess
    Closure onFailure = { String fileName, Status status ->
        MessageDialog.openError(controller?.shell, "Error", "Can't save file ${fileName}; ${status.message}")
    }
    
    SaveTestCaseAsAction() {
        super("Save Test Case As")
        //setAccelerator(SWT.MOD1 + (int)'S' )
        setToolTipText("Save Test Case As")
    }
    
    SaveTestCaseAsAction(controller) {
        this()
        this.controller = controller
    }
    
    void run() {
        testCase = testCase ?: controller?.model?.selectedTestCase
        if(testCase) {
            def shell = controller.shell
            String fileName
            if(testCase.file == null) {
                FileDialog dlg = new FileDialog(shell, SWT.SAVE)
                dlg.filterNames = ["Test Case (*.html)", "All Files (*.*)"]
                dlg.filterExtensions = [ "*.html", "*.*" ]
                if(testCase.file) {
                    dlg.fileName = testCase.file.name
                    dlg.filterPath = testCase.file.parent
                }
                
                boolean done = false
                while (!done) {
                    fileName = dlg.open()
                    if (fileName == null) {
                        done = true;
                    } else {
                        File file = new File(fileName)
                        if (file.exists()) {
                            MessageBox mb = new MessageBox(shell, SWT.ICON_WARNING | SWT.YES | SWT.NO)
                            mb.setMessage(fileName + " already exists. Do you want to replace it?")
                            done = ( mb.open() == SWT.YES )
                        } else {
                            done = true
                        }
                    }
                }
            } else {
                fileName = testCase?.path
            }
            if (fileName != null) {
                testCase.file = new File(fileName)
                Status status = testCase.save()
                if( !status.success ) {
                    onFailure?.call(fileName, status)
                } else {
                    onSuccess?.call(fileName, status)
                }
            }
        }
        
    }
}
