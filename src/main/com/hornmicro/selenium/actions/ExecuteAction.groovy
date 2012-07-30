package com.hornmicro.selenium.actions

import java.util.concurrent.Callable
import java.util.concurrent.CancellationException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

import org.eclipse.jface.action.Action
import org.eclipse.swt.graphics.Color
import org.eclipse.swt.graphics.RGB
import org.eclipse.swt.widgets.Display
import org.eclipse.swt.widgets.Table

import com.hornmicro.selenium.driver.DriveTest
import com.hornmicro.selenium.model.TestCaseModel
import com.hornmicro.selenium.model.TestModel
import com.hornmicro.selenium.ui.MainController
import com.hornmicro.selenium.ui.Resources
import com.hornmicro.selenium.ui.MainView.Log

class ExecuteAction extends Action implements Callable<Void> {
    static final ExecutorService executor 
    static { executor = Executors.newSingleThreadExecutor() }
    static Future<Void> future
    
    MainController controller
    Color red = Resources.getColor(new RGB(0xFF, 0xCC, 0xCC))
    Color yellow = Resources.getColor(new RGB(0xFF, 0xFF, 0xCC))
    Color green = Resources.getColor(new RGB(0xEE, 0xFF, 0xEE))
    Closure onSuccess
    Closure onError
    Closure onCancelled
    Boolean clearProgress = true
    
    Log log
    TestCaseModel tcm
    TestModel tm
    Table table
    
    
    ExecuteAction(MainController controller, Boolean clearProgress=true, 
            Closure onSuccess=null, Closure onError=null, Closure onCancelled=null) {
        super("E&xecute this command")
        setAccelerator((int)'x' )
        setToolTipText("Execute the currently selected command")
        this.controller = controller
        this.clearProgress = clearProgress
        this.onSuccess = onSuccess
        this.onError = onError
        this.onCancelled = onCancelled
    }
    
    void run() {
        log = controller.view.log
        tcm = controller.model?.selectedTestCase
        tm = tcm?.selectedTest
        
        if(tm) {
            
            // Cancel another thread if already running
            cancel()
            
            // Highlight row
            table = controller.view.testCaseViewer.table
            Table testCasesTable = controller.view.testCasesViewer.table
            
            if(clearProgress) {
                (0..<table.getItemCount()).each { 
                    table.getItem(it).setBackground(null)
                }
            }
            int index = tcm.tests.indexOf(tm)
            table.getItem(index).setBackground(yellow)
            table.setSelection(-1)
            testCasesTable.setEnabled(false)
            
            log.info("Executing: | ${tm.command} | ${tm.target} | ${tm.value} |")
            future = executor.submit(this) // this schedule the call() method
            Thread.start {
                try {
                    future.get(10, TimeUnit.SECONDS)    
                } catch(TimeoutException te) {
                    future.cancel(true)
                    Display.default.asyncExec {
                        log.error("Timed out executing")
                    }
                } catch(CancellationException ce) {
                    Display.default.asyncExec {
                        log.error("Cancelled: ${ce.message}")
                    }
                } finally {
                    Display.default.asyncExec {
                        testCasesTable.setEnabled(true)
                    }
                }
            }
        }
    }
    
    Void call() {
        Table table = controller.view.testCaseViewer.table
        int index = tcm.tests.indexOf(tm)
        
        try {
            DriveTest.executeAction(controller.model.browser, tcm.baseURL, tm)
            Display.default.asyncExec {
                table.getItem(index).setBackground(green)
                onSuccess?.call()
            }
        } catch(e) {
            System.err.println(e.message)
            Display.default.asyncExec {
                log.error(e.message)
                table.getItem(index).setBackground(red)
                onError?.call()
            }
        } finally {
            Display.default.asyncExec {
                if(index + 1 < table.getItemCount()) {
                    index++
                }
                //table.setSelection(index)
            }
        }
    }
    
    void cancel() {
        if(future && !future.isDone()) {
            future.cancel(true)
            onCancelled?.call()
        }
    }
    
    void dispose() {
        executor.shutdownNow()
    }
}