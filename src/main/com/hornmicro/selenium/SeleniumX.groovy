package com.hornmicro.selenium

import org.eclipse.swt.widgets.Display

import com.hornmicro.selenium.ui.MainController

class SeleniumX {
    MainController controller
    
    public SeleniumX() {
        Display.appName = "selenium.x"
        
        controller = new MainController()
    }
    
    void run() {
        controller.run()
    }
    
    static main(args) {
        new SeleniumX().run()
    }

}
