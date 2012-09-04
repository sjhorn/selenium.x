package com.hornmicro.selenium.actions

import org.eclipse.jface.action.Action
import org.eclipse.swt.SWT
import org.eclipse.swt.dnd.TextTransfer

import com.hornmicro.selenium.model.TestCaseModel
import com.hornmicro.selenium.model.TestModel
import com.hornmicro.selenium.ui.MainController

class PasteAction extends Action {
    MainController controller
    
    PasteAction(MainController controller) {
        super("Paste")
        setAccelerator(SWT.MOD1 + (int)'V' )
        setToolTipText("Paste")
        
        this.controller = controller
    }
    
    void run() {
        
        TextTransfer transfer = TextTransfer.getInstance()
        String data = (String) controller.clipBoard.getContents(transfer)
        if (data != null) {
            List<TestModel> tests = TestModel.parse(data)
            
            TestCaseModel tcm = controller.model.selectedTestCase
            if(tests?.size() && tcm) {
                if(tcm.selectedTest) {
                    tcm.tests.addAll(tcm.tests.indexOf(tcm.selectedTest), tests)
                } else {
                    tcm.tests.addAll(tcm.tests.size(), tests)
                }
                controller.view.testCaseViewer.refresh()
            }
        }
    }

}
