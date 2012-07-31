package com.hornmicro.selenium.actions

import java.util.concurrent.CancellationException
import java.util.concurrent.ExecutionException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

import org.codehaus.groovy.runtime.StackTraceUtils
import org.eclipse.jface.action.Action
import org.eclipse.swt.graphics.Color
import org.eclipse.swt.graphics.RGB
import org.eclipse.swt.widgets.Display
import org.eclipse.swt.widgets.Table

import com.hornmicro.selenium.driver.DriveTest
import com.hornmicro.selenium.model.TestCaseModel
import com.hornmicro.selenium.model.TestModel
import com.hornmicro.selenium.model.TestState
import com.hornmicro.selenium.ui.MainController
import com.hornmicro.selenium.ui.Resources
import com.hornmicro.selenium.ui.MainView.Log

class ExecuteAction extends Action {
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
    Boolean runOne = true
    
    Log log
    TestCaseModel tcm
    TestModel tm
    Table table
    
    
    ExecuteAction(MainController controller, Boolean clearProgress=true, Boolean runOne=true,
            Closure onSuccess=null, Closure onError=null, Closure onCancelled=null) {
        super("E&xecute this command")
        setAccelerator((int)'x' )
        setToolTipText("Execute the currently selected command")
        this.controller = controller
        this.clearProgress = clearProgress
        this.runOne = runOne
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
            
            // Hide selection
            controller.view.testCaseViewer.table.setSelection(-1)
            
            if(clearProgress) {
                tcm.state = runOne ? TestState.UNKNOWN : TestState.INPROGRESS
                tcm.tests.each { TestModel tm_it ->
                    tm_it.state = TestState.UNKNOWN
                }
                controller.view.testCasesViewer.refresh()
            }
            tm.state = TestState.INPROGRESS
            controller.view.testCaseViewer.refresh()
            
            log.info("Executing: | ${tm.command} | ${tm.target} | ${tm.value} |")
            
            future = executor.submit {
                DriveTest.executeAction(controller.model.browser, tcm.baseURL, tm)
            }
            Thread.start {
                try {
                    future.get(10, TimeUnit.SECONDS)
                    Display.default.asyncExec {
                        tm.state = TestState.SUCCESS
                        onSuccess?.call()
                    }
                } catch(ExecutionException ee) {
                    StackTraceUtils.deepSanitize(ee)
                    Display.default.asyncExec {
                        log.error(ee.cause.message)
                        tm.state = TestState.FAILED
                        onError?.call()
                    }
                } catch(TimeoutException te) {
                    Display.default.asyncExec {
                        log.error("Timed out")
                        tm.state = TestState.FAILED
                        onError?.call()
                    }
                } catch(CancellationException ce) {
                    Display.default.asyncExec {
                        log.error("Cancelled: ${ce.message}")
                        tm.state = TestState.UNKNOWN
                        onCancelled.call()
                    }
                } finally {
                    Display.default.asyncExec {
                        controller.view.testCaseViewer.refresh()
                    }
                }
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