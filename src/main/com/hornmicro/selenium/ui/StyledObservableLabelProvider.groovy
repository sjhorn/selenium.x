package com.hornmicro.selenium.ui

import org.eclipse.core.databinding.observable.map.IMapChangeListener
import org.eclipse.core.databinding.observable.map.IObservableMap
import org.eclipse.core.databinding.observable.map.MapChangeEvent
import org.eclipse.jface.viewers.LabelProviderChangedEvent
import org.eclipse.jface.viewers.StyledCellLabelProvider
import org.eclipse.swt.graphics.Image


class StyledObservableLabelProvider extends StyledCellLabelProvider implements IMapChangeListener {
    protected IObservableMap[] attributeMaps

    public void handleMapChange(MapChangeEvent event) {
        Set affectedElements = event.diff.getChangedKeys()
        LabelProviderChangedEvent newEvent = new LabelProviderChangedEvent(this, affectedElements.toArray())
        fireLabelProviderChanged(newEvent)
    }

    public StyledObservableLabelProvider(IObservableMap attributeMap) {
        this([ attributeMap ] as IObservableMap[] )
    }
    
    StyledObservableLabelProvider() {
        
    }
    
    public setObservableMap(IObservableMap[] attributeMaps) {
        System.arraycopy(attributeMaps, 0,
                this.attributeMaps = new IObservableMap[attributeMaps.length],
                0, attributeMaps.length)
        for (int i = 0; i < attributeMaps.length; i++) {
            attributeMaps[i].addMapChangeListener(this)
        }
    }

    public void dispose() {
        for (int i = 0; i < attributeMaps.length; i++) {
            attributeMaps[i].removeMapChangeListener(this)
        }
        super.dispose()
        this.attributeMaps = null
    }

    public Image getImage(Object element) {
        return getColumnImage(element, 0)
    }

    public String getText(Object element) {
        return getColumnText(element, 0)
    }

    public Image getColumnImage(Object element, int columnIndex) {
        return null
    }

    public String getColumnText(Object element, int columnIndex) {
        if (columnIndex < attributeMaps.length) {
            Object result = attributeMaps[columnIndex].get(element)
            return result == null ? "" : result.toString()
        }
        return null
    }
}
