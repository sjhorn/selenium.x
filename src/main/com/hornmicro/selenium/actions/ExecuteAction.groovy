package com.hornmicro.selenium.actions

import org.eclipse.jface.action.Action
import org.eclipse.swt.SWT
import org.eclipse.swt.widgets.Display
import org.eclipse.swt.widgets.Table

import com.hornmicro.selenium.driver.DriveTest
import com.hornmicro.selenium.model.TestCaseModel
import com.hornmicro.selenium.model.TestModel

class ExecuteAction extends Action {
    def controller
    
    ExecuteAction(controller) {
        super("E&xecute this command")
        setAccelerator((int)'x' )
        setToolTipText("Execute the currently selected command")
        this.controller = controller
    }
    
    void run() {
        TestCaseModel tcm = controller.model?.selectedTestCase
        TestModel tm = tcm?.selectedTest
        if(tm) {
            // Highlight row
            Table table = controller.view.testCaseViewer.table
            
            def index = table.getSelectionIndex()
            def background = table.getItem(index).getBackground()
            table.getItem(index).setBackground(Display.default.getSystemColor(SWT.COLOR_YELLOW))
            table.setSelection(-1)
            
            println "Would call ${tcm.baseURL} -> ${tm.command}('${tm.target}', '${tm.value}')"
            Thread.start {
                DriveTest.executeAction(tcm.baseURL, tm.command, tm.target, tm.value)
                
                Display.default.asyncExec {
                    table.getItem(index).setBackground(background)
                }
            }
            
        }
    }
}
