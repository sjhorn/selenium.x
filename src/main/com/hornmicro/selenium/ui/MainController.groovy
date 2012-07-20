package com.hornmicro.selenium.ui

import java.beans.PropertyChangeListener

import org.codehaus.groovy.runtime.StackTraceUtils
import org.eclipse.core.databinding.DataBindingContext
import org.eclipse.core.databinding.beans.BeanProperties
import org.eclipse.core.databinding.beans.PojoProperties
import org.eclipse.core.databinding.observable.IObservable
import org.eclipse.core.databinding.observable.list.WritableList
import org.eclipse.core.databinding.observable.masterdetail.IObservableFactory
import org.eclipse.core.databinding.observable.value.ComputedValue
import org.eclipse.jface.action.Action
import org.eclipse.jface.action.MenuManager
import org.eclipse.jface.databinding.swt.WidgetProperties
import org.eclipse.jface.databinding.swt.WidgetValueProperty
import org.eclipse.jface.databinding.viewers.IViewerObservableValue
import org.eclipse.jface.databinding.viewers.ViewerProperties
import org.eclipse.jface.databinding.viewers.ViewerSupport
import org.eclipse.jface.viewers.StructuredSelection
import org.eclipse.jface.viewers.StyledCellLabelProvider
import org.eclipse.jface.viewers.ViewerCell
import org.eclipse.jface.window.ApplicationWindow
import org.eclipse.jface.window.Window
import org.eclipse.jface.window.Window.IExceptionHandler
import org.eclipse.swt.SWT
import org.eclipse.swt.events.DisposeEvent
import org.eclipse.swt.events.DisposeListener
import org.eclipse.swt.events.MouseAdapter
import org.eclipse.swt.events.MouseEvent
import org.eclipse.swt.events.SelectionAdapter
import org.eclipse.swt.events.SelectionEvent
import org.eclipse.swt.layout.FillLayout
import org.eclipse.swt.widgets.Composite
import org.eclipse.swt.widgets.Control
import org.eclipse.swt.widgets.Display
import org.eclipse.swt.widgets.Shell
import org.eclipse.swt.widgets.ToolItem

import com.hornmicro.selenium.actions.AddTestCaseAction
import com.hornmicro.selenium.actions.ExecuteAction
import com.hornmicro.selenium.actions.FindAction
import com.hornmicro.selenium.actions.OpenAction
import com.hornmicro.selenium.actions.ReloadAction
import com.hornmicro.selenium.actions.RemoveTestCaseAction
import com.hornmicro.selenium.driver.DriveTest
import com.hornmicro.selenium.model.TestCaseModel
import com.hornmicro.selenium.model.TestModel
import com.hornmicro.selenium.model.TestSuiteModel

class MainController extends ApplicationWindow implements Runnable, Window.IExceptionHandler, DisposeListener {
    Action openAction
    Action executeAction
    Action findAction
    Action reloadAction
    Action addTestCaseAction
    Action removeTestCaseAction
    
    TestSuiteModel model = new TestSuiteModel()
    MainView view
    def parent
    
    public MainController() {
        super(null)
        
        openAction = new OpenAction(this)
        executeAction = new ExecuteAction(this)
        findAction = new FindAction(this)
        
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
        DriveTest.dispose()
        Resources.dispose()
    }
    
    protected void configureShell(Shell shell) {
        super.configureShell(shell)
        shell.text = "selenium.x"
        shell.setSize(740, 700)
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
    
    class TestCaseProvider extends StyledCellLabelProvider {
        void update(ViewerCell cell) {
            TestModel test = cell.element
            def col = cell.columnIndex
            
            cell.text = 
                col == 0 ? test.command : 
                col == 1 ? test.target :
                col == 2 ? test.value : ""
            
            cell.setBackground(Display.default.getSystemColor(SWT.COLOR_YELLOW))    
            super.update(cell)
        }
    }
    
    static class ToolbarProperties extends WidgetValueProperty {
        static selection() {
            return new ToolbarProperties()
        }
        
        private ToolbarProperties() {
            super(SWT.Selection);
        }
    
        boolean doGetBooleanValue(Object source) {
            return ((ToolItem) source).getSelection();
        }
    
        void doSetBooleanValue(Object source, boolean value) {
            ((ToolItem) source).setSelection(value);
        }
    
        String toString() {
            return "Toolbar.selection <Boolean>"
        }
        
        Object getValueType() {
            return Boolean.TYPE;
        }
    
        Object doGetValue(Object source) {
            return doGetBooleanValue(source) ? Boolean.TRUE : Boolean.FALSE;
        }
    
        void doSetValue(Object source, Object value) {
            if (value == null)
                value = Boolean.FALSE;
            doSetBooleanValue(source, ((Boolean) value).booleanValue());
        }
    }
    
    
    void wireView() {
        DataBindingContext dbc = new DataBindingContext()
        
        // Bind the selected browser to the TestSuiteModel
        dbc.bindValue(
            new ComputedValue() {
                Object calculate() {
                    def firefox = ToolbarProperties.selection().observe(view.firefox) 
                    def chrome = ToolbarProperties.selection().observe(view.firefox)
                    
                    if(firefox.getValue()) {
                        return "firefox"
                    } else if(chrome.getValue()) {
                        return "chrome"
                    }
                    return "chrome"
                }
            },
            BeanProperties.value("browser").observe(model)
        )
        
        
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
        //view.testCaseViewer.labelProvider(new TestCaseProvider())
        
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
            selection,
            BeanProperties.value("selectedTestCase").value("selectedTest").observe(model)
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
        
        // Listen to the Find Button
        view.findTarget.addSelectionListener(new SelectionAdapter() {
            void widgetSelected(SelectionEvent se) {
                findAction.run()
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
        MenuManager actionsMenu = new MenuManager("Actions")
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
        menuManager.add(actionsMenu)
        actionsMenu.add(executeAction)
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
