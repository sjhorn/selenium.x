package com.hornmicro.selenium.ui

import org.eclipse.swt.graphics.Image
import org.eclipse.swt.widgets.Display

class ImageFactory {
    static Map imageCache = [:]
    
    static Image getImage(path) {
        if(!imageCache.containsKey(path)) {
            Display.default.syncExec {
                imageCache[path] = new Image(Display.default, path)
            }
        }
        return imageCache[path]
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
