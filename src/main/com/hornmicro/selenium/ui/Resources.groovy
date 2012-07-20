package com.hornmicro.selenium.ui

import org.eclipse.swt.graphics.Color
import org.eclipse.swt.graphics.Image
import org.eclipse.swt.widgets.Display

class Resources {
    static Map imageCache = [:]
    static Map colorCache = [:]
    
    static Image getImage(path) {
        if(!imageCache.containsKey(path)) {
            Display.default.syncExec {
                imageCache[path] = new Image(Display.default, path)
            }
        }
        return imageCache[path]
    }
    
    static Color getColor(color) {
        if(!colorCache.containsKey(color)) {
            Display.default.syncExec {
                colorCache[color] = new Color(Display.default, color)
            }
        }
        return colorCache[color]
    }
    
    static dispose() {
        Display.Default.syncExec{
            imageCache.each { path, image ->
                image.dispose()
            }
            imageCache = [:]
        }
    }
}
