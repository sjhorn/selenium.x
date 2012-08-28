package com.hornmicro.selenium.actions

import org.eclipse.jface.action.Action
import org.eclipse.jface.dialogs.MessageDialog
import org.eclipse.swt.SWT
import org.eclipse.swt.widgets.FileDialog
import org.eclipse.swt.widgets.MessageBox

import com.hornmicro.selenium.model.Status
import com.hornmicro.selenium.model.TestCaseModel
import com.hornmicro.selenium.model.TestSuiteModel
import com.hornmicro.selenium.ui.MainController

class SaveTestSuiteAsAction extends Action {
    MainController controller
    File file
    
    SaveTestSuiteAsAction(MainController controller, File file = null) {
        super("Save Test Suite As")
        //setAccelerator(SWT.MOD1 + (int)'S' )
        setToolTipText("Save Test Suite As")
        
        this.controller = controller
        this.file = file
    }
    
    void run() {
        TestSuiteModel testSuite = controller.model
        if(testSuite) {
            String fileName
            if(file == null) {
                FileDialog dlg = new FileDialog(shell, SWT.SAVE)
                dlg.filterNames = ["Test Suite (*.html)", "All Files (*.*)"]
                dlg.filterExtensions = [ "*.html", "*.*" ]
                if(testSuite.file) {
                    dlg.fileName = testSuite.file.name
                    dlg.filterPath = testSuite.file.parent
                }
                
                boolean done = false
                while (!done) {
                    fileName = dlg.open()
                    if (fileName == null) {
                        done = true;
                    } else {
                        File file = new File(fileName)
                        if (file.exists()) {
                            MessageBox mb = new MessageBox(controller.shell, SWT.ICON_WARNING | SWT.YES | SWT.NO)
                            mb.setMessage(fileName + " already exists. Do you want to replace it?")
                            done = ( mb.open() == SWT.YES )
                        } else {
                            done = true
                        }
                    }
                }
            } else {
                fileName = file?.path
            }
            if (fileName != null) {
                testSuite.file = new File(fileName)
                List statusList = []
                Status status = testSuite.save()
                if(!status.success) {
                    statusList.add(status)
                }
                
                testSuite.testCases.each { TestCaseModel testCase ->
                    status = testCase.save()
                    if(!status.success) {
                        statusList.add(status)
                    }
                }
                
                if( statusList.size() ) {
                    MessageDialog.openError(
                        controller.shell, 
                        "Error saving Test Suite", 
                        "There were some issues saving testsuite.\n\n"+ statusList.errors.join("\n"))
                }
            }
        }
    }
}
