package com.hornmicro.selenium.actions

import org.eclipse.jface.action.Action
import org.eclipse.swt.SWT

import com.hornmicro.selenium.model.TestCaseModel

class NewTestCaseAction extends Action {
    def controller 
    
    NewTestCaseAction(controller) {
        super("New Test Case")
        setAccelerator(SWT.MOD1 + (int)'N' )
        setToolTipText("New Test Case")
        
        this.controller = controller    
    }
    
    void run() {
        def model = controller.model
        def names = model.testCases.collect { it.name }
        
        model.testCases += new TestCaseModel(name: getNextName(names))
    }
    
    String getNextName(names) {
        def count = 1
        
        // Keeping adding 1 to Untitled until unique
        while( "Untitled ${count > 1 ? count : ''}".trim() in names ) {
            count++
        }
        return "Untitled ${count > 1 ? count : ''}".trim()
    }
}
