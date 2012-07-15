package com.hornmicro.selenium

import net.miginfocom.swt.MigLayout

import org.eclipse.swt.SWT
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.widgets.Display
import org.eclipse.swt.widgets.Event
import org.eclipse.swt.widgets.Listener
import org.eclipse.swt.widgets.Sash
import org.eclipse.swt.widgets.Shell
import org.eclipse.swt.widgets.Text

class Test {

    public static void main(String[] args) {
        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setLayout(new MigLayout("inset 0, gap 0","[fill][][grow, fill]", "[grow, fill]"))
        
        final Sash sash = new Sash(shell, SWT.BORDER | SWT.VERTICAL);
        sash.layoutData = "cell 1 0"
        //sash.setBounds(10, 10, 32, 100);
        //sash.setBackground(Display.default.getSystemColor(SWT.COLOR_BLACK))
        
        
        final def text = new Text(shell, SWT.BORDER)
        text.layoutData = "w 300, cell 0 0"
        
        sash.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event e) {
                def x = e.x < 100 ? 100 : e.x 
                text.layoutData = "w ${x}, cell 0 0".toString()
                sash.setBounds(x, e.y, e.width, e.height);
                shell.layout()
            }
        });
        sash.addControlListener(new ControlAdapter() {
            public void controlResized() {
                
            }
        })
    
        def text2 = new Text(shell, SWT.BORDER )
        text2.layoutData = "growx, cell 2 0"
    
    
        shell.open();
        sash.setFocus();
        while (!shell.isDisposed()) {
          if (!display.readAndDispatch())
            display.sleep();
        }
        display.dispose();
    }

}
