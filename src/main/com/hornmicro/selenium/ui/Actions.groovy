package com.hornmicro.selenium.ui

import groovy.transform.CompileStatic

import org.eclipse.jface.action.Action
import org.eclipse.jface.action.IAction
import org.eclipse.jface.util.IPropertyChangeListener
import org.eclipse.jface.util.PropertyChangeEvent
import org.eclipse.swt.SWT
import org.eclipse.swt.events.DisposeEvent
import org.eclipse.swt.events.DisposeListener
import org.eclipse.swt.layout.FillLayout
import org.eclipse.swt.widgets.Button
import org.eclipse.swt.widgets.Control
import org.eclipse.swt.widgets.Display
import org.eclipse.swt.widgets.Event
import org.eclipse.swt.widgets.Listener
import org.eclipse.swt.widgets.Shell
import org.eclipse.swt.widgets.Widget

@CompileStatic
class Actions implements Listener, DisposeListener, IPropertyChangeListener {
    int eventId
    Control control
    Action action
    
    static Actions selection(Control control) {
        return new Actions(control: control, eventId: SWT.Selection)
    }
    
    void connect(Action action) {
        this.action = action
        control.addListener(eventId, this)
        control.addDisposeListener(this)
        action.addPropertyChangeListener(this)
    }
     
        
    public void handleEvent(Event event) {
        action.run()
    }

    void widgetDisposed(DisposeEvent event) {
        control?.removeListener(eventId, this)
    }

    void propertyChange(PropertyChangeEvent pe) {
        switch(pe.getProperty()) {
            case IAction.ENABLED:
                control.setEnabled(pe.getNewValue() as boolean)
                break
            case IAction.IMAGE:
                println "Image changed to ${pe.getNewValue()}"
                break
        }
    }
    
    
    static class TestAction extends Action {
        void run() {
            println "w00ted"
            setEnabled(false)
        }
    }
    static main(args) {
        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setLayout(new FillLayout())
        
        shell.setSize(800,600)
        Button button = new Button(shell, SWT.PUSH)
        button.text = "w00t"
        
        TestAction ta = new TestAction()
        
        Actions.selection(button).connect(ta)

        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch())
                display.sleep();
        }
        display.dispose();
    }
}
