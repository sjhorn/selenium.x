package com.hornmicro.selenium.ui

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked

import org.eclipse.swt.SWT
import org.eclipse.swt.widgets.Composite
import org.eclipse.swt.widgets.Display
import org.eclipse.swt.widgets.Label

@CompileStatic
class ToolLabels {
    Composite parent
    
    ToolLabels(Composite parent) {
        this.parent = parent    
    }
    
    Label label(Map<String, String> options) {
        Label label = new Label(parent, SWT.NONE)
        label.toolTipText = options['tip']
        label.image = Resources.getImage(options['image']) 
        label.cursor = Display.getDefault().getSystemCursor(SWT.CURSOR_HAND)
        return label
    }
}
