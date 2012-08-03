package com.hornmicro.selenium.ui

import groovy.transform.CompileStatic

import org.eclipse.jface.action.Action
import org.eclipse.jface.action.IAction
import org.eclipse.jface.resource.ImageDescriptor
import org.eclipse.jface.util.IPropertyChangeListener
import org.eclipse.jface.util.PropertyChangeEvent
import org.eclipse.swt.SWT
import org.eclipse.swt.events.DisposeEvent
import org.eclipse.swt.events.DisposeListener
import org.eclipse.swt.graphics.Image
import org.eclipse.swt.layout.FillLayout
import org.eclipse.swt.widgets.Button
import org.eclipse.swt.widgets.Display
import org.eclipse.swt.widgets.Event
import org.eclipse.swt.widgets.Listener
import org.eclipse.swt.widgets.Shell
import org.eclipse.swt.widgets.Widget

@CompileStatic
class Actions implements Listener, DisposeListener, IPropertyChangeListener {
    int eventId
    Widget widget
    Action action
    
    static Actions selection(Widget widget) {
        return new Actions(widget: widget, eventId: SWT.Selection)
    }
    
    void connect(Action action) {
        this.action = action
        widget.addListener(eventId, this)
        widget.addDisposeListener(this)
        action.addPropertyChangeListener(this)
    }
     
        
    public void handleEvent(Event event) {
        action.run()
    }

    void widgetDisposed(DisposeEvent event) {
        widget?.removeListener(eventId, this)
    }

    void propertyChange(PropertyChangeEvent pe) {
        switch(pe.getProperty()) {
            case IAction.ENABLED:
                try {
                    widget.invokeMethod("setEnabled", pe.getNewValue() as boolean)
                } catch(e) {
                    // We can only try
                }
                break
            case IAction.IMAGE:
                if( pe.getNewValue() instanceof ImageDescriptor) {
                    try {
                        ImageDescriptor id = (ImageDescriptor) pe.getNewValue()
                        widget.invokeMethod("setImage", id.createImage())
                    } catch(e) {
                        // We can only try. 
                    }
                }
                break
        }
    }
    
    static class TestAction extends Action {
        void run() {
            println "w00ted"
            setEnabled(false)
            setImageDescriptor(ImageDescriptor.createFromImage(new Image(Display.getDefault(), "gfx/safari.png")))
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
