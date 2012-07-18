package com.hornmicro.selenium.actions

import org.eclipse.jface.action.Action
import org.eclipse.jface.dialogs.MessageDialog
import org.eclipse.swt.SWT
import org.eclipse.swt.widgets.FileDialog

import com.hornmicro.selenium.model.Status
import com.hornmicro.selenium.model.TestSuiteModel

class OpenAction extends Action {
    def controller
    
    OpenAction(controller) {
        super("&Open")
        setAccelerator(SWT.MOD1 + (int)'O' )
        setToolTipText("Open")
        
        this.controller = controller
    }
    
    void run() {
        TestSuiteModel model = controller.model
        def shell = controller.shell

        FileDialog dlg = new FileDialog(shell, SWT.OPEN)
        dlg.filterNames = ["All Files (*.*)"]
        dlg.filterExtensions = ["*.*"]
        String fileName = dlg.open()
        
        if (fileName != null /*&& checkOverwrite(model, shell) */ ) {
            Status status = model.open(new File(fileName))
            if(!status.success) {
                MessageDialog.openError(shell, "Open Failed", status.message)
            }
        }
        
    }
/*
    boolean checkOverwrite(model, shell) {
        def proceed = true;
        if (model.isDirty()) {
            proceed = MessageDialog.openConfirm(shell, "Are you sure?",
                    "You have unsaved changes, are you sure you want to lose them?");
        }
        return proceed;
    }
*/
}

/*
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebDriverBackedSelenium
import org.openqa.selenium.firefox.FirefoxDriver

import com.thoughtworks.selenium.Selenium
import groovy.xml.*
import org.ccil.cowan.tagsoup.Parser

class DriveTest {
    List methods = Selenium.declaredMethods*.name
    
    static assertClosure(method, truth=true) {
        switch(method.parameterTypes.size()) {
            case 0: return { -> assert method() == truth }
            case 1: return { arg1 -> assert method(arg1) == truth }
            case 2: return { arg1, arg2 -> assert method(arg1, arg2) == truth }
            default:
                throw new RuntimeException("Dont know this method + ${method.name}")
        }
    }
    
    static waitForClosure(method, truth=true) {
        
    }
    
    static andWaitClosure(method) {
        
    }
    
    static getMethod(name) {
        
    }
    
    static main(args) {
        //WebDriver driver = new FirefoxDriver();
        try {
            String baseUrl = "http://www.wotif.com/";
            
            //Selenium selenium = new WebDriverBackedSelenium(driver, baseUrl);
    
            def html = new XmlParser(new Parser()).parse(new File("/Users/shorn/dev/functional-testing/anz/Property Details/Currency Converter.html"))
            
            html.'**'.tr.each { tr ->
                if(tr.children().size() == 3) {
                    def method = tr.td[0].text()
                    
                   
                }
                
            }
            
            
            
            //selenium.open("hotel/View?hotel=W4937");
            //assert selenium.isTextPresent("Cancellation policy: No Cancellations or Changes") 
            //selenium.click("link=Details");
            //assert selenium.isVisible("cancelPolicy0")
            
            //println "PASSED WOOHOO !!!"
        } finally {
            //driver.quit()
        }
        
    }

}
 
 */ 
