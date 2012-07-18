package com.hornmicro.selenium

import groovy.transform.CompileStatic

import org.eclipse.core.databinding.observable.Realm
import org.eclipse.jface.databinding.swt.SWTObservables
import org.eclipse.swt.widgets.Display

import com.hornmicro.selenium.ui.MainController


class SeleniumX implements Runnable {
    Display display
    MainController controller
    
    public SeleniumX() {
        Display.appName = "selenium.x"
        display = new Display()
        controller = new MainController()
    }
    
    void run() {
        Realm.runWithDefault(SWTObservables.getRealm(display), controller)
    }
    
    static main(args) {
         new SeleniumX().run()
    }

}
