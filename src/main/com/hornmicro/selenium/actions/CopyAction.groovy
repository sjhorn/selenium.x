package com.hornmicro.selenium.actions

import org.eclipse.jface.action.Action
import org.eclipse.swt.SWT
import org.eclipse.swt.dnd.TextTransfer
import org.eclipse.swt.dnd.Transfer

import com.hornmicro.selenium.model.TestModel
import com.hornmicro.selenium.ui.MainController

class CopyAction extends Action {
    MainController controller
    
    CopyAction(MainController controller) {
        super("Copy")
        setAccelerator(SWT.MOD1 + (int)'C' )
        setToolTipText("Copy")
        
        this.controller = controller
    }
    
    void run() {
        if(controller.commandSelection?.size()) {
            StringBuilder textData = new StringBuilder()
            controller.commandSelection.each { TestModel command ->
                textData.append("${command.toString()}\n")
            }
            
            TextTransfer textTransfer = TextTransfer.getInstance()
            controller.clipBoard.setContents(
                [textData.toString()] as Object[], 
                [textTransfer] as Transfer[]
            )
        }
    }
}
