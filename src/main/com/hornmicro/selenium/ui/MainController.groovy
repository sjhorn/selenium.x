package com.hornmicro.selenium.ui

import org.codehaus.groovy.runtime.StackTraceUtils
import org.eclipse.core.databinding.DataBindingContext
import org.eclipse.core.databinding.beans.BeanProperties
import org.eclipse.core.databinding.beans.BeansObservables
import org.eclipse.core.databinding.beans.PojoProperties
import org.eclipse.jface.action.Action
import org.eclipse.jface.action.MenuManager
import org.eclipse.jface.databinding.swt.WidgetProperties
import org.eclipse.jface.databinding.viewers.IViewerObservableValue
import org.eclipse.jface.databinding.viewers.ViewerProperties
import org.eclipse.jface.databinding.viewers.ViewerSupport
import org.eclipse.jface.window.ApplicationWindow
import org.eclipse.jface.window.Window
import org.eclipse.jface.window.Window.IExceptionHandler
import org.eclipse.swt.SWT
import org.eclipse.swt.layout.FillLayout
import org.eclipse.swt.widgets.Composite
import org.eclipse.swt.widgets.Control
import org.eclipse.swt.widgets.Display
import org.eclipse.swt.widgets.Shell

import com.hornmicro.selenium.actions.OpenAction
import com.hornmicro.selenium.actions.ReloadAction

class MainController extends ApplicationWindow implements Runnable, Window.IExceptionHandler {
    Action openAction
    Action reloadAction
    
    TestSuiteModel model = new TestSuiteModel()
    MainView view
    def parent
    
    public MainController() {
        super(null)
        
        openAction = new OpenAction(this)
        reloadAction = new ReloadAction(this)
        
        addMenuBar()
        setExceptionHandler(this)
    }
    
    void run() {
        blockOnOpen = true
        open()
        Display.current?.dispose()
    }
    
    protected void configureShell(Shell shell) {
        super.configureShell(shell)
        shell.text = "selenium.x"
        shell.setSize(740, 600)
    }
    
    protected Control createContents(Composite parent) {
        this.parent = parent
        parent.setLayout(new FillLayout())
        view = new MainView(parent, SWT.NONE)
        view.createContents()
        
        wireView()
        return view
    }
    
    void wireView() {
        DataBindingContext dbc = new DataBindingContext()
        
        ViewerSupport.bind(
            view.testCasesViewer,
            BeansObservables.observeList(model, "testCases"), // list of items
            BeanProperties.values(["name"] as String[]) // labels
        )
        
        ViewerSupport.bind(
            view.testCaseViewer,
            ViewerProperties.singleSelection()
                .list(BeanProperties.list("tests", ObservableList.class))
                .observe(view.testCasesViewer),
            BeanProperties.values(["command", "target", "value"] as String[]) // labels
        )
        
        IViewerObservableValue selection = ViewerProperties.singleSelection().observe(view.testCaseViewer)
        
        dbc.bindValue(
            WidgetProperties.selection().observe(view.command),
            PojoProperties.value("command", String).observeDetail(selection)
        )
        
        dbc.bindValue(
            WidgetProperties.selection().observe(view.target),
            PojoProperties.value("target", String).observeDetail(selection)
        )
        
        dbc.bindValue(
            WidgetProperties.text(SWT.Modify).observe(view.value),
            PojoProperties.value("value", String).observeDetail(selection)
        )
        
    }
    
    void reload() {
        view.dispose()
        view = new MainView(parent, SWT.NONE)
        view.createContents()
        
        model = new TestSuiteModel()
        
        wireView()
        parent.layout()
    }
    
    protected MenuManager createMenuManager() {
        MenuManager menuManager = new MenuManager();
        MenuManager fileMenu = new MenuManager("File")
        MenuManager editMenu = new MenuManager("Edit")
        MenuManager helpMenu = new MenuManager("Help")
    
        menuManager.add(fileMenu)
        fileMenu.add(openAction)
        
        /*
        fileMenu.add(saveAction)
        fileMenu.add(saveAsAction)
        fileMenu.add(closeAction)
        */
        menuManager.add(editMenu)
        editMenu.add(reloadAction)
        /*
        editMenu.add(undoAction)
        editMenu.add(redoAction)
        editMenu.add(new Separator())
        editMenu.add(copyAction)
        editMenu.add(cutAction)
        editMenu.add(pasteAction)
        editMenu.add(new Separator())
        editMenu.add(selectAllAction)
        */
        return menuManager
    }

    public void handleException(Throwable e) {
        StackTraceUtils.deepSanitize(e)
        e.printStackTrace()
    }

}
