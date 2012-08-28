package com.hornmicro.selenium.actions

import groovy.lang.Closure;

import org.eclipse.jface.action.Action
import org.eclipse.jface.dialogs.MessageDialog
import org.eclipse.swt.SWT

import com.hornmicro.selenium.model.Status
import com.hornmicro.selenium.model.TestCaseModel
import com.hornmicro.selenium.model.TestSuiteModel
import com.hornmicro.selenium.ui.MainController


class SaveTestCaseAction extends Action {
    MainController controller
    TestCaseModel testCase
    Closure onSuccess
    Closure onFailure = { String fileName, Status status ->
        MessageDialog.openError(controller?.shell, "Error", "Can't save file ${fileName}; ${status.message}")
    }
    
    SaveTestCaseAction() {
        super("Save Test Case")
        setAccelerator(SWT.MOD1 + (int)'S' )
        setToolTipText("Save Test Case")
    }
    
    SaveTestCaseAction(MainController controller) {
        this()
        this.controller = controller
    }
    
    void run() {
        testCase = testCase ?: controller?.model?.selectedTestCase
        if(testCase) {
            if(testCase.file == null) {
                new SaveTestCaseAsAction(
                    controller: controller, 
                    onSuccess: onSuccess, 
                    onFailure: onFailure
                ).run()
            } else {
                Status status = testCase.save()
                if( !status.success ) {
                    onFailure?.call(testCase.file.name, status)
                } else {
                    onSuccess?.call(testCase.file.name, status)
                }
            }
        }
    } 
    
}
