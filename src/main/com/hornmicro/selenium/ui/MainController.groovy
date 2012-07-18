package com.hornmicro.selenium.ui

import java.beans.PropertyChangeListener

import org.codehaus.groovy.runtime.StackTraceUtils
import org.eclipse.core.databinding.DataBindingContext
import org.eclipse.core.databinding.beans.BeanProperties
import org.eclipse.core.databinding.beans.PojoProperties
import org.eclipse.core.databinding.observable.IObservable
import org.eclipse.core.databinding.observable.list.IObservableList
import org.eclipse.core.databinding.observable.list.WritableList
import org.eclipse.core.databinding.observable.masterdetail.IObservableFactory
import org.eclipse.core.databinding.observable.masterdetail.MasterDetailObservables
import org.eclipse.jface.action.Action
import org.eclipse.jface.action.MenuManager
import org.eclipse.jface.databinding.swt.WidgetProperties
import org.eclipse.jface.databinding.viewers.IViewerObservableValue
import org.eclipse.jface.databinding.viewers.ViewerProperties
import org.eclipse.jface.databinding.viewers.ViewerSupport
import org.eclipse.jface.viewers.StructuredSelection
import org.eclipse.jface.window.ApplicationWindow
import org.eclipse.jface.window.Window
import org.eclipse.jface.window.Window.IExceptionHandler
import org.eclipse.swt.SWT
import org.eclipse.swt.events.DisposeEvent
import org.eclipse.swt.events.DisposeListener
import org.eclipse.swt.events.MouseAdapter
import org.eclipse.swt.events.MouseEvent
import org.eclipse.swt.layout.FillLayout
import org.eclipse.swt.widgets.Composite
import org.eclipse.swt.widgets.Control
import org.eclipse.swt.widgets.Display
import org.eclipse.swt.widgets.Shell

import com.hornmicro.selenium.actions.AddTestCaseAction
import com.hornmicro.selenium.actions.OpenAction
import com.hornmicro.selenium.actions.ReloadAction
import com.hornmicro.selenium.actions.RemoveTestCaseAction
import com.hornmicro.selenium.model.TestCaseModel;
import com.hornmicro.selenium.model.TestSuiteModel;

class MainController extends ApplicationWindow implements Runnable, Window.IExceptionHandler, DisposeListener {
    Action openAction
    Action reloadAction
    Action addTestCaseAction
    Action removeTestCaseAction
    
    TestSuiteModel model = new TestSuiteModel()
    MainView view
    def parent
    
    public MainController() {
        super(null)
        
        openAction = new OpenAction(this)
        reloadAction = new ReloadAction(this)
        addTestCaseAction = new AddTestCaseAction(this)
        removeTestCaseAction = new RemoveTestCaseAction(this)
        
        addMenuBar()
        setExceptionHandler(this)
    }
    
    void run() {
        blockOnOpen = true
        open()
        Display.default?.dispose()
    }
    
    void widgetDisposed(DisposeEvent de) {
        ImageFactory.dispose()
    }
    
    protected void configureShell(Shell shell) {
        super.configureShell(shell)
        shell.text = "selenium.x"
        shell.setSize(740, 600)
        shell.addDisposeListener(this)
    }
    
    protected Control createContents(Composite parent) {
        this.parent = parent
        parent.setLayout(new FillLayout())
        view = new MainView(parent, SWT.NONE)
        view.createContents()
        
        wireView()
        
        model.open(new File("/Users/shorn/dev/selenium.x/test/Test Suite"))
        
        return view
    }
    
    void wireView() {
        DataBindingContext dbc = new DataBindingContext()
        
        // Bind TestSuite name to shell title
        dbc.bindValue(
            PojoProperties.value("text").observe(view.shell),
            BeanProperties.value("name").observe(model)
        )
        
        // Connect testCases to Test Case table
        ViewerSupport.bind(
            view.testCasesViewer,
            BeanProperties.list("testCases", ObservableList).observe(model), // list of testCases
            BeanProperties.values(["name"] as String[]) // labels
        )
        
        // Observe test case selection
        IViewerObservableValue testCaseSelection = ViewerProperties.singleSelection().observe(view.testCasesViewer)
        
        // Connect testCase selection to Table of commands
        ViewerSupport.bind(
            view.testCaseViewer,
            BeanProperties.list("tests", ObservableList).observeDetail(testCaseSelection),
            BeanProperties.values(["command", "target", "value"] as String[]) // labels
        )
        
        // Connect the current test case selection to the TestSuite model. 
        dbc.bindValue(
            testCaseSelection,
            BeanProperties.value("selectedTestCase", TestCaseModel).observe(model) 
        )
        
        // Bind list of BaseURLs from the testcases
        dbc.bindList(
            WidgetProperties.items().observe(view.baseURL),
            BeanProperties.list("testCases").values("baseURL").observe(model)
        )
        
        // Store the values edited back in list
        dbc.bindValue(
            WidgetProperties.text().observe(view.baseURL),
            BeanProperties.value("baseURL", String).observeDetail(testCaseSelection)
        )

        // Observe the current command selection
        IViewerObservableValue selection = ViewerProperties.singleSelection().observe(view.testCaseViewer)
        
        dbc.bindValue(
            BeanProperties.value("selectedTest").observe(model.selectedTestCase),
            selection
        )
        
        // Two-way link the current command to command text
        dbc.bindValue(
            WidgetProperties.selection().observe(view.command),
            PojoProperties.value("command", String).observeDetail(selection)
        )
        
        // Two-way link the current command to target text
        dbc.bindValue(
            WidgetProperties.selection().observe(view.target),
            PojoProperties.value("target", String).observeDetail(selection)
        )
        
        // Two-way link the current command to value text
        dbc.bindValue(
            WidgetProperties.text(SWT.Modify).observe(view.value),
            PojoProperties.value("value", String).observeDetail(selection)
        )
        
        // Selenese Source
        dbc.bindValue(
            BeanProperties.value("selenese", String).observeDetail(testCaseSelection),
            WidgetProperties.text(SWT.Modify).observe(view.source),
        )
         
        // Listen to the add test case tool
        view.addTestCase.addMouseListener(new MouseAdapter() {
            void mouseUp(MouseEvent e) {
                MainController.this.addTestCaseAction.run()
            }
        })
        
        // Listen to the remove test case tool
        view.removeTestCase.addMouseListener(new MouseAdapter() {
            void mouseUp(MouseEvent e) {
                MainController.this.removeTestCaseAction.run()
            }
        })
        
        // Select the first command when we change test cases
        model.addPropertyChangeListener("selectedTestCase", [ propertyChange: { e ->
            if(model?.selectedTestCase?.tests?.size()) { 
                Display.default.asyncExec { 
                    view.testCaseViewer.setSelection(new StructuredSelection(model.selectedTestCase.tests[0]), true);
                }
            }
        }] as PropertyChangeListener)
        
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
    
    // Cleanup the groovy proxy noise in the exception
    public void handleException(Throwable e) {
        StackTraceUtils.deepSanitize(e)
        e.printStackTrace()
    }

}

class ObservableListFactory implements IObservableFactory {
    public IObservable createObservable(Object target) {
        WritableList list = WritableList.withElementType(String)
        list.addAll((Collection) target?.baseURL)
        return list
    }
}
