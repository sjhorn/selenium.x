package com.hornmicro.selenium.ui

import org.eclipse.jface.databinding.swt.WidgetValueProperty
import org.eclipse.swt.SWT
import org.eclipse.swt.widgets.ToolItem

class ToolbarProperties extends WidgetValueProperty {
    private ToolbarProperties() {
        super(SWT.Selection)
    }
    
    static ToolbarProperties selection() {
        return new ToolbarProperties()
    }

    boolean doGetBooleanValue(Object source) {
        return ((ToolItem) source).getSelection()
    }

    void doSetBooleanValue(Object source, boolean value) {
        ((ToolItem) source).setSelection(value)
    }

    String toString() {
        return "Toolbar.selection <Boolean>"
    }

    Object getValueType() {
        return Boolean.TYPE
    }

    Object doGetValue(Object source) {
        return doGetBooleanValue(source) ? Boolean.TRUE : Boolean.FALSE
    }

    void doSetValue(Object source, Object value) {
        if (value == null)
            value = Boolean.FALSE
        doSetBooleanValue(source, ((Boolean) value).booleanValue())
    }
}
