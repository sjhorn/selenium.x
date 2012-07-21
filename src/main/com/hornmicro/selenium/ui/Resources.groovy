package com.hornmicro.selenium.ui

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked

import org.eclipse.swt.graphics.Color
import org.eclipse.swt.graphics.Image
import org.eclipse.swt.graphics.RGB
import org.eclipse.swt.widgets.Display
import java.util.Map.Entry

@CompileStatic
class Resources {
    static final Map<String, Image> imageCache = [:]
    static final Map<RGB, Color> colorCache = [:]
    
    static Image getImage(String path) {
        if(!imageCache.containsKey(path)) {
            Display.getDefault().syncExec {
                imageCache[path] = new Image(Display.getDefault(), path)
            }
        }
        return imageCache[path]
    }
    
    static Color getColor(RGB color) {
        if(!colorCache.containsKey(color)) {
            Display.getDefault().syncExec {
                colorCache[color] = new Color(Display.getDefault(), color)
            }
        }
        return colorCache[color]
    }
    
    static void dispose() {
        Display.getDefault().syncExec{
            imageCache.each { Entry<String, Image> entry ->
                entry.getValue().dispose()
            }
            imageCache.clear()
            colorCache.each { Entry<RGB, Color> entry ->
                entry.getValue().dispose() 
            }
            colorCache.clear()
        }
    }
    
}
