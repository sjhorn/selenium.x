package com.hornmicro.selenium

import org.eclipse.core.databinding.observable.Realm
import org.eclipse.jface.databinding.swt.SWTObservables
import org.eclipse.swt.widgets.Display

import com.hornmicro.selenium.ui.MainController

class SeleniumX implements Runnable {
    MainController controller
    
    public SeleniumX() {
        Display.appName = "selenium.x"
        controller = new MainController()
    }
    
    void run() {
        controller.run()
    }
    
    static main(args) {
        Realm.runWithDefault(SWTObservables.getRealm(new Display()), new SeleniumX())
    }

}
