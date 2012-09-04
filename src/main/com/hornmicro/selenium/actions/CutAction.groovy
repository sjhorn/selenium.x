package com.hornmicro.selenium.actions

import org.eclipse.jface.action.Action
import org.eclipse.swt.SWT
import org.eclipse.swt.dnd.TextTransfer
import org.eclipse.swt.dnd.Transfer

import com.hornmicro.selenium.model.TestModel
import com.hornmicro.selenium.ui.MainController

class CutAction extends Action {
    MainController controller
    
    CutAction(MainController controller) {
        super("Cut")
        setAccelerator(SWT.MOD1 + (int)'X' )
        setToolTipText("Cut")
        
        this.controller = controller
    }
    
    void run() {
        if(controller.commandSelection?.size() && controller.model.selectedTestCase) {
            StringBuilder textData = new StringBuilder()
            controller.commandSelection.each { TestModel command ->
                textData.append("${command.toString()}\n")
            }
            
            TextTransfer textTransfer = TextTransfer.getInstance()
            controller.clipBoard.setContents(
                [textData.toString()] as Object[],
                [textTransfer] as Transfer[]
            )
            
            controller.model.selectedTestCase.tests -= controller.commandSelection
        }
    }

}
