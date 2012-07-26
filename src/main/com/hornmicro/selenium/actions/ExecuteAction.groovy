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
import com.hornmicro.selenium.ui.MainController
import com.hornmicro.selenium.ui.Resources

class ExecuteAction extends Action {
    MainController controller
    Color red = Resources.getColor(new RGB(0xFF, 0xCC, 0xCC))
    Color yellow = Resources.getColor(new RGB(0xFF, 0xFF, 0xCC))
    Color green = Resources.getColor(new RGB(0xEE, 0xFF, 0xEE))
    Closure callback
    Boolean clearProgress = true
    
    ExecuteAction(MainController controller,Boolean clearProgress=true,Closure callback=null) {
        super("E&xecute this command")
        setAccelerator((int)'x' )
        setToolTipText("Execute the currently selected command")
        this.controller = controller
        this.clearProgress = clearProgress
        this.callback = callback
    }
    
    void run() {
        def log = controller.view.log
        TestSuiteModel model = controller.model
        TestCaseModel tcm = controller.model?.selectedTestCase
        TestModel tm = tcm?.selectedTest
        if(tm) {
            // Highlight row
            Table table = controller.view.testCaseViewer.table
            
            def index = tcm.tests.indexOf(tm)
            if(clearProgress) {
                (0..<table.getItemCount()).each { 
                    table.getItem(it).setBackground(null)
                }
            }
            //def index = table.getSelectionIndex()
            //def background = table.getItem(index).getBackground()
            table.getItem(index).setBackground(yellow)
            table.setSelection(-1)
            
            log.info("Executing: | ${tm.command} | ${tm.target} | ${tm.value} |")
            Thread.start {
                try {
                    DriveTest.executeAction(model.browser, tcm.baseURL, tm)
                    Display.default.asyncExec {
                        table.getItem(index).setBackground(green)
                        callback?.call()
                    }
                } catch(e) {
                    System.err.println(e.message) 
                    //e.printStackTrace()
                    Display.default.asyncExec {
                        log.error(e.message)
                        table.getItem(index).setBackground(red)
                    }
                } finally {
                    Display.default.asyncExec {
                        if(index + 1 < table.getItemCount()) {
                            index++
                        }
                        table.setSelection(index)
                    }
                }
            }
            
        }
    }
}
