package com.hornmicro.selenium.ui

import groovy.transform.CompileStatic

import org.codehaus.groovy.runtime.StackTraceUtils
import org.eclipse.core.databinding.DataBindingContext
import org.eclipse.core.databinding.beans.BeanProperties
import org.eclipse.core.databinding.beans.PojoProperties
import org.eclipse.core.databinding.observable.list.IObservableList
import org.eclipse.core.databinding.observable.value.ComputedValue
import org.eclipse.core.databinding.observable.value.WritableValue
import org.eclipse.core.databinding.property.Properties
import org.eclipse.core.databinding.property.value.IValueProperty
import org.eclipse.jface.action.Action
import org.eclipse.jface.action.MenuManager
import org.eclipse.jface.action.Separator
import org.eclipse.jface.databinding.swt.WidgetProperties
import org.eclipse.jface.databinding.viewers.IViewerObservableValue
import org.eclipse.jface.databinding.viewers.ObservableListContentProvider
import org.eclipse.jface.databinding.viewers.ViewerProperties
import org.eclipse.jface.fieldassist.AutoCompleteField
import org.eclipse.jface.fieldassist.ComboContentAdapter
import org.eclipse.jface.viewers.StructuredSelection
import org.eclipse.jface.viewers.StructuredViewer
import org.eclipse.jface.viewers.ViewerCell
import org.eclipse.jface.window.ApplicationWindow
import org.eclipse.jface.window.Window
import org.eclipse.jface.window.Window.IExceptionHandler
import org.eclipse.swt.SWT
import org.eclipse.swt.dnd.Clipboard
import org.eclipse.swt.events.DisposeEvent
import org.eclipse.swt.events.DisposeListener
import org.eclipse.swt.events.MouseAdapter
import org.eclipse.swt.events.MouseEvent
import org.eclipse.swt.events.SelectionAdapter
import org.eclipse.swt.events.SelectionEvent
import org.eclipse.swt.graphics.Color
import org.eclipse.swt.graphics.Rectangle
import org.eclipse.swt.layout.FillLayout
import org.eclipse.swt.widgets.Composite
import org.eclipse.swt.widgets.Control
import org.eclipse.swt.widgets.Display
import org.eclipse.swt.widgets.Shell

import com.hornmicro.selenium.actions.AddCommandAction
import com.hornmicro.selenium.actions.CopyAction
import com.hornmicro.selenium.actions.CutAction
import com.hornmicro.selenium.actions.ExecuteAction
import com.hornmicro.selenium.actions.FindAction
import com.hornmicro.selenium.actions.InsertCommandAction
import com.hornmicro.selenium.actions.NewTestCaseAction
import com.hornmicro.selenium.actions.NewTestSuiteAction
import com.hornmicro.selenium.actions.OpenAction
import com.hornmicro.selenium.actions.PasteAction
import com.hornmicro.selenium.actions.PauseResumeAction
import com.hornmicro.selenium.actions.PlayAllAction
import com.hornmicro.selenium.actions.PlayCurrentAction
import com.hornmicro.selenium.actions.ReloadAction
import com.hornmicro.selenium.actions.RemoveCommandAction
import com.hornmicro.selenium.actions.RemoveTestCaseAction
import com.hornmicro.selenium.actions.ResetBrowsersAction
import com.hornmicro.selenium.actions.SaveTestCaseAction
import com.hornmicro.selenium.actions.SaveTestCaseAsAction
import com.hornmicro.selenium.actions.SaveTestSuiteAction
import com.hornmicro.selenium.actions.SaveTestSuiteAsAction
import com.hornmicro.selenium.driver.DriveTest
import com.hornmicro.selenium.driver.SeleneseAPI
import com.hornmicro.selenium.model.RunState
import com.hornmicro.selenium.model.TestCaseModel
import com.hornmicro.selenium.model.TestModel
import com.hornmicro.selenium.model.TestState
import com.hornmicro.selenium.model.TestSuiteModel


class MainController extends ApplicationWindow implements Runnable, Window.IExceptionHandler, DisposeListener {
    static SeleneseAPI seleneseAPI = new SeleneseAPI()
    Clipboard clipBoard
    IObservableList commandSelection
    Action newTestSuiteAction
    Action saveTestSuiteAsAction
    Action saveTestSuiteAction
    Action saveTestCaseAsAction
    Action saveTestCaseAction
    Action executeAction
    Action openAction
    Action findAction
    
    Action reloadAction
    Action resetBrowsersAction
    
    Action newTestCaseAction
    Action removeTestCaseAction
    Action addCommandAction
    Action insertCommandAction
    Action removeCommandAction
    
    Action copyAction
    Action cutAction
    Action pasteAction
    
    PauseResumeAction pauseResumeAction
    PlayCurrentAction playCurrentAction
    Action playAllAction
    
    
    TestSuiteModel model = new TestSuiteModel()
    MainView view
    Composite parent
    
    public MainController() {
        super(null)
        clipBoard = new Clipboard(Display.getDefault())
        openAction = new OpenAction(this)
        saveTestCaseAction = new SaveTestCaseAction(this)
        saveTestCaseAsAction = new SaveTestCaseAsAction(this)
        
        newTestSuiteAction = new NewTestSuiteAction(this)
        saveTestSuiteAction = new SaveTestSuiteAction(this)
        saveTestSuiteAsAction = new SaveTestSuiteAsAction(this)
        
        executeAction = new ExecuteAction(this)
        findAction = new FindAction(this)
        
        reloadAction = new ReloadAction(this)
        resetBrowsersAction = new ResetBrowsersAction()
        
        newTestCaseAction = new NewTestCaseAction(this)
        removeTestCaseAction = new RemoveTestCaseAction(this)
        
        cutAction = new CutAction(this)
        copyAction = new CopyAction(this)
        pasteAction = new PasteAction(this)
        
        addCommandAction = new AddCommandAction(this)
        insertCommandAction = new InsertCommandAction(this)
        removeCommandAction = new RemoveCommandAction(this)
        
        playCurrentAction = new PlayCurrentAction(this)
        pauseResumeAction = new PauseResumeAction(this)
        playAllAction = new PlayAllAction(this)
        
        addMenuBar()
        setExceptionHandler(this)
    }
    
    @CompileStatic
    void run() {
        blockOnOpen = true
        open()
        Display.getDefault()?.dispose()

        // Safari driver does not release threads so force exit :(
        System.exit(0)
    }
    
    @CompileStatic
    void widgetDisposed(DisposeEvent de) {
        clipBoard.dispose()
        DriveTest.dispose()
        Resources.dispose()
    }
    
    @CompileStatic
    protected void configureShell(Shell shell) {
        super.configureShell(shell)
        shell.text = "selenium.x"
        shell.setImage(Resources.getImage("gfx/Selenium_x.png"))
        Rectangle displayBounds = Display.getDefault().getBounds()
        shell.setBounds(displayBounds.width - 740, 1, 740, 1000 > displayBounds.height ? displayBounds.height : 1000)
        shell.addDisposeListener(this)
    }
    
    @CompileStatic
    protected Control createContents(Composite parent) {
        this.parent = parent
        parent.setLayout(new FillLayout())
        view = new MainView(parent, SWT.NONE)
        view.createContents()
        
        wireView()
        setRunning(RunState.STOPPED)
        model.open(new File("test/Property Details/Test Suite"))
        
        return view
    }
    
    class TestLabelProvider extends StyledObservableLabelProvider {
        void update(ViewerCell cell) {
            def col = cell.columnIndex
            def test = cell.element
            if(test instanceof TestModel) {
                cell.text = 
                    col == 0 ? test.command : 
                    col == 1 ? test.target :
                    col == 2 ? test.value : ""
            } else {
                cell.text = test.name
            }
            
            Color cellBg  
            switch(test.state) {
                case TestState.INPROGRESS:
                    cellBg = Resources.getColor(0xFF, 0xFF, 0xCC) // yellow
                    break
                case TestState.SUCCESS:
                    cellBg =  Resources.getColor(0xEE, 0xFF, 0xEE) // green
                    break
                case TestState.FAILED:
                    cellBg =  Resources.getColor(0xFF, 0xCC, 0xCC) // red
                    break
                default: 
                    cellBg = null
                    break
            }
            cell.setBackground(cellBg)
            super.update(cell)
        }
        
    }
    
    void setRunning(RunState state) {
        executeAction.setEnabled(state != RunState.RUNNING)
        playAllAction.setEnabled(state != RunState.RUNNING)
        playCurrentAction.setEnabled(state != RunState.RUNNING)
        pauseResumeAction.setEnabled(state != RunState.STOPPED)
    }
    
    void wireView() {
        DataBindingContext dbc = new DataBindingContext()
        
        dbc.bindValue(
            BeanProperties.value("delay").observe(model),
            WidgetProperties.selection().observe(view.scale)
        )
        
        Actions.selection(view.playCurrent).connect(playCurrentAction)
        Actions.selection(view.pauseResume).connect(pauseResumeAction)
        Actions.selection(view.playAll).connect(playAllAction)
        
        // Bind the selected browser to the TestSuiteModel
        dbc.bindValue(
            new ComputedValue() {
                Object calculate() {
                    def htmlunit = ToolbarProperties.selection().observe(view.htmlunit) 
                    def firefox = ToolbarProperties.selection().observe(view.firefox) 
                    def chrome = ToolbarProperties.selection().observe(view.chrome)
                    def safari = ToolbarProperties.selection().observe(view.safari)
                    def opera = ToolbarProperties.selection().observe(view.opera)
                    def iphone = ToolbarProperties.selection().observe(view.iphone)
                    def ie8 = ToolbarProperties.selection().observe(view.ie8)
                    def android = ToolbarProperties.selection().observe(view.android)
                    if(htmlunit.getValue()) {
                        return "htmlunit"
                    } else if(firefox.getValue()) {
                        return "firefox"
                    } else if(chrome.getValue()) {
                        return "chrome"
                    } else if(safari.getValue()) {
                        return "safari"
                    } else if(opera.getValue()) {
                        return "opera"
                    } else if(iphone.getValue()) {
                        return "iphone"
                    } else if(ie8.getValue()) {
                        return "ie8"
                    } else if(android.getValue()) {
                        return "android"
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
        bind(
            view.testCasesViewer,
            BeanProperties.list("testCases", ObservableList).observe(model), // list of testCases
            BeanProperties.values(["name"] as String[]), // labels
            new TestLabelProvider()
        )
        
        dbc.bindValue(
            WidgetProperties.text().observe(view.runs),
            BeanProperties.value("runs").observe(model)
        )
        
        dbc.bindValue(
            WidgetProperties.text().observe(view.failures),
            BeanProperties.value("failures").observe(model)
        )
        
        // Observe test case selection
        IViewerObservableValue testCaseSelection = ViewerProperties.singleSelection().observe(view.testCasesViewer)
        
        // Connect testCase selection to Table of commands
        bind(
            view.testCaseViewer,
            BeanProperties.list("tests", ObservableList).observeDetail(testCaseSelection),
            BeanProperties.values(["command", "target", "value"] as String[]), // labels
            new TestLabelProvider()
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

        commandSelection = ViewerProperties.multipleSelection().observe(view.testCaseViewer)
        
        // Observe the current command selection
        IViewerObservableValue selection = ViewerProperties.singleSelection().observe(view.testCaseViewer)
        
        dbc.bindValue(
            selection,
            BeanProperties.value("selectedTestCase").value("selectedTest").observe(model)
        )
        
        // Two-way link the current command to command text
        def commandSelection = WidgetProperties.selection().observe(view.command)
        dbc.bindValue(
            commandSelection,
            PojoProperties.value("command", String).observeDetail(selection)
        )
        dbc.bindList(
            WidgetProperties.items().observe(view.command),
            Properties.selfList().observe(seleneseAPI.commandNames)
        )
        new AutoCompleteField(view.command, new ComboContentAdapter(), seleneseAPI.commandNames as String[])
        dbc.bindValue(
            commandSelection,
            new WritableValue() {
                void doSetValue(Object sel) {
                    if(sel) {
                        view.setReferenceHTML(seleneseAPI.getReferenceFor(sel) ?: '')
                    }
                }
            }
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
                MainController.this.newTestCaseAction.run()
            }
        })
        
        // Listen to the remove test case tool
        view.removeTestCase.addMouseListener(new MouseAdapter() {
            void mouseUp(MouseEvent e) {
                MainController.this.removeTestCaseAction.run()
            }
        })
        
        // Listen to the add test tool
        view.addTest.addMouseListener(new MouseAdapter() {
            void mouseUp(MouseEvent e) {
                MainController.this.addCommandAction.run()
            }
        })
        
        // Listen to the remove test tool
        view.removeTest.addMouseListener(new MouseAdapter() {
            void mouseUp(MouseEvent e) {
                MainController.this.removeCommandAction.run()
            }
        })
        
        // Listen to the Find Button
        view.findTarget.addSelectionListener(new SelectionAdapter() {
            void widgetSelected(SelectionEvent se) {
                findAction.run()
            }
        })
        
        // Select the first command when we change test cases
        dbc.bindValue(
            ViewerProperties.singleSelection().observe(view.testCasesViewer),
            new WritableValue() {
                void doSetValue(Object sel) {
                    if(model?.selectedTestCase?.tests?.size()) {
                        view.testCaseViewer.setSelection(new StructuredSelection(model.selectedTestCase.tests[0]))
                    }
                }
            }
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
    
    @CompileStatic
    protected MenuManager createMenuManager() {
        MenuManager menuManager = new MenuManager();
        MenuManager fileMenu = new MenuManager("File")
        MenuManager editMenu = new MenuManager("Edit")
        MenuManager actionsMenu = new MenuManager("Actions")
        MenuManager helpMenu = new MenuManager("Help")
    
        menuManager.add(fileMenu)
        fileMenu.add(openAction)
        fileMenu.add(new Separator())
        fileMenu.add(newTestCaseAction)
        fileMenu.add(saveTestCaseAction)
        fileMenu.add(saveTestCaseAsAction)
        fileMenu.add(new Separator())
        fileMenu.add(newTestSuiteAction)
        fileMenu.add(saveTestSuiteAction)
        fileMenu.add(saveTestSuiteAsAction)
        
        /*
        fileMenu.add(saveAction)
        fileMenu.add(saveAsAction)
        fileMenu.add(closeAction)
        */
        menuManager.add(editMenu)
        editMenu.add(reloadAction)
        editMenu.add(resetBrowsersAction)
        editMenu.add(new Separator())
        editMenu.add(cutAction)
        editMenu.add(copyAction)
        editMenu.add(pasteAction)
        editMenu.add(new Separator())
        editMenu.add(newTestCaseAction)
        editMenu.add(removeTestCaseAction)
        editMenu.add(new Separator())
        editMenu.add(insertCommandAction)
        editMenu.add(addCommandAction)
        editMenu.add(removeCommandAction)
        /*
        editMenu.add(undoAction)
        editMenu.add(redoAction)
        editMenu.add(new Separator())
        editMenu.add(cutAction)
        editMenu.add(pasteAction)
        editMenu.add(new Separator())
        editMenu.add(selectAllAction)
        */
        menuManager.add(actionsMenu)
        actionsMenu.add(playAllAction)
        actionsMenu.add(playCurrentAction)
        actionsMenu.add(pauseResumeAction)
        actionsMenu.add(new Separator())
        actionsMenu.add(executeAction)
        return menuManager
    }
    
    // Cleanup the groovy proxy noise in the exception
    @CompileStatic
    public void handleException(Throwable e) {
        StackTraceUtils.deepSanitize(e)
        e.printStackTrace()
    }
    
    @CompileStatic
    static void bind(StructuredViewer viewer, IObservableList input, IValueProperty[] labelProperties, StyledObservableLabelProvider labelProvider) {
        ObservableListContentProvider contentProvider = new ObservableListContentProvider()
        if (viewer.getInput() != null)
            viewer.setInput(null);
        viewer.setContentProvider(contentProvider)
        labelProvider.setObservableMap(Properties.observeEach(contentProvider.getKnownElements(), labelProperties))
        viewer.setLabelProvider(labelProvider)
        if (input != null)
            viewer.setInput(input)
    }
}

