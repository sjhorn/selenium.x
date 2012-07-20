package com.hornmicro.selenium.actions

import org.eclipse.jface.action.Action
import org.eclipse.swt.graphics.Color
import org.eclipse.swt.graphics.RGB
import org.eclipse.swt.widgets.Display
import org.eclipse.swt.widgets.Table

import com.hornmicro.selenium.driver.DriveTest
import com.hornmicro.selenium.model.TestCaseModel
import com.hornmicro.selenium.model.TestModel
import com.hornmicro.selenium.model.TestSuiteModel
import com.hornmicro.selenium.ui.Resources

class ExecuteAction extends Action {
    def controller
    Color red = Resources.getColor(new RGB(0xFF, 0xCC, 0xCC))
    Color yellow = Resources.getColor(new RGB(0xFF, 0xFF, 0xCC))
    Color green = Resources.getColor(new RGB(0xEE, 0xFF, 0xEE))
    
    ExecuteAction(controller) {
        super("E&xecute this command")
        setAccelerator((int)'x' )
        setToolTipText("Execute the currently selected command")
        this.controller = controller
    }
    
    void run() {
        def log = controller.view.log
        TestSuiteModel model = controller.model
        TestCaseModel tcm = controller.model?.selectedTestCase
        TestModel tm = tcm?.selectedTest
        if(tm) {
            // Highlight row
            Table table = controller.view.testCaseViewer.table
            
            def index = table.getSelectionIndex()
            def background = table.getItem(index).getBackground()
            table.getItem(index).setBackground(yellow)
            table.setSelection(-1)
            
            log.info("Executing: | ${tm.command} | ${tm.target} | ${tm.value} |")
            Thread.start {
                try {
                    DriveTest.executeAction(model.browser, tcm.baseURL, tm.command, tm.target, tm.value)
                    Display.default.asyncExec {
                        table.getItem(index).setBackground(green)
                    }
                } catch(e) {
                    Display.default.asyncExec {
                        log.error(e.message)
                        table.getItem(index).setBackground(red)
                    }
                }
            }
            
        }
    }
}
